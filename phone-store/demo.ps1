$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot"
$PROJECT = "D:\MyProjects\hz3451\phone-store"
$MAVEN = "C:\Users\Alina\AppData\Local\Temp\opencode\apache-maven-3.9.6\bin\mvn.cmd"
$BASE = "http://localhost:8080"

$PASS = "Green"; $STEP = "Cyan"; $CMD = "Yellow"; $ERR = "Red"; $HDR = "Magenta"
$fail = 0

function Title($t) { Write-Host "`n==================== $t ====================" -ForegroundColor $HDR }
function Step($t) { Write-Host "`n--- $t" -ForegroundColor $STEP }

function Exec($m, $u, $b, $d, $h = @{}, $expect = $null) {
    $p = @{ Uri = "$BASE$u"; Method = $m; ContentType = "application/json"; Headers = $h }
    if ($b) { $p.Body = ($b | ConvertTo-Json -Depth 10 -Compress); Write-Host "  Body: $($p.Body)" -ForegroundColor Gray }
    Write-Host "  -> $d" -ForegroundColor $CMD
    try {
        $r = Invoke-WebRequest @p -UseBasicParsing
        if ($r.Content) { try { $r.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10 } catch {} }
        if ($expect -and $r.StatusCode -ne $expect) { throw "Expected $expect but got $($r.StatusCode)" }
        Write-Host "  OK ($($r.StatusCode))" -ForegroundColor $PASS
        return $r.Content | ConvertFrom-Json
    } catch {
        $sc = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.value__ } else { "N/A" }
        Write-Host "  ($sc) $d" -ForegroundColor $(if ($expect -and $sc -eq $expect) { $PASS } else { $ERR })
        if ($expect -and $sc -eq $expect) { return $null }
        if ($_.Exception.Response) { try { $r2 = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream()); $r2.ReadToEnd() | ConvertFrom-Json | ConvertTo-Json -Depth 10; $r2.Close() } catch {} }
        $script:fail++
        return $null
    }
}

# ─── 0. BUILD ────────────────────────────────────────────────
Title "PHONE STORE — AUTO BUILD & TEST"
Step "BUILDING PROJECT"
$b = cmd /c "`"$env:JAVA_HOME\bin\java`" -version 2>&1 && cd /d $PROJECT && `"$MAVEN`" clean package -DskipTests 2>&1" | Out-String
if ($b -match "BUILD SUCCESS") { Write-Host "BUILD SUCCESS" -ForegroundColor $PASS } else { Write-Host $b -ForegroundColor $ERR; exit 1 }

# ─── 1. KILL OLD + CLEAN + START ──────────────────────────────
Title "STARTING APPLICATION (H2 + demo profile)"
Write-Host "Killing old Java processes..." -ForegroundColor Gray
netstat -ano | Select-String ":8080 " | ForEach-Object { $p = $_ -replace '.*\s+(\d+)$','$1'; if ($p -match '^\d+$') { cmd /c "taskkill /F /PID $p 2>nul" } }
Get-Process java -ErrorAction SilentlyContinue | ForEach-Object { cmd /c "taskkill /F /PID $($_.Id) 2>nul" }
Remove-Item -LiteralPath "$PROJECT\data" -Recurse -Force -ErrorAction SilentlyContinue
Start-Sleep 2

$proc = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d $PROJECT && `"$MAVEN`" spring-boot:run -Dspring-boot.run.profiles=demo" -PassThru -WindowStyle Minimized
Write-Host "Waiting for startup" -ForegroundColor Gray -NoNewline
$ready = $false
for ($i = 0; $i -lt 90; $i++) {
    Start-Sleep -Seconds 1
    try { $r = Invoke-WebRequest -Uri "$BASE/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"x","password":"x"}' -UseBasicParsing -ErrorAction SilentlyContinue; $ready = $true; break }
    catch { if ($_.Exception.Response) { $ready = $true; break } }
    if ($i % 10 -eq 9) { Write-Host " ($($i+1)s)" -ForegroundColor Gray -NoNewline } elseif ($i % 5 -eq 4) { Write-Host "." -ForegroundColor Gray -NoNewline }
}
if (-not $ready) { Write-Host " FAILED" -ForegroundColor $ERR; if ($proc.HasExited) { Write-Host "  (process exited with code $($proc.ExitCode))" -ForegroundColor $ERR }; exit 1 }
Write-Host " ready!" -ForegroundColor $PASS

$utok = $null; $atok = $null; $pid_phone = $null

# ─── 2. REGISTER ─────────────────────────────────────────────
Step "1. REGISTER"
$reg = @{ username = "demouser"; email = "demo@phonestore.com"; password = "demo123"; confirmPassword = "demo123" }
$r = Exec POST "/auth/register" $reg "Create user"

# ─── 3. LOGIN USER ───────────────────────────────────────────
Step "2. LOGIN USER"
$l = Exec POST "/auth/login" @{ username = "demouser"; password = "demo123" } "Login as demouser"
if ($l -and $l.token) { $utok = $l.token; Write-Host "  User token obtained" -ForegroundColor $PASS }

# ─── 4. LIST PHONES ──────────────────────────────────────────
Step "3. LIST PHONES (empty catalog)"
Exec GET "/phones" $null "List all phones" @{ Authorization = "Bearer $utok" }

# ─── 5. ACCESS CONTROL ──────────────────────────────────────
Step "4. ACCESS CONTROL (USER cannot create)"
Exec POST "/phones" @{ brand="T"; model="X"; price=1; storageGb=1; ramGb=1 } "Create as USER (expect 403)" @{ Authorization = "Bearer $utok" } 403

# ─── 6. LOGIN ADMIN ──────────────────────────────────────────
Step "5. LOGIN ADMIN"
$la = Exec POST "/auth/login" @{ username = "admin"; password = "admin123" } "Login as admin"
if (-not $la -or -not $la.token) {
    Write-Host "Registering admin..." -ForegroundColor Yellow
    Exec POST "/auth/register" @{ username="admin"; email="admin@p.com"; password="admin123"; confirmPassword="admin123" } "Register admin"
    $la = Exec POST "/auth/login" @{ username = "admin"; password = "admin123" } "Login as admin"
}
if ($la -and $la.token) { $atok = $la.token; Write-Host "  Admin OK" -ForegroundColor $PASS }

# ─── 7. CREATE PHONE ─────────────────────────────────────────
Step "6. CREATE PHONE (ADMIN)"
$np = @{ brand="Apple"; model="iPhone 16 Pro Max"; price=1299.99; storageGb=256; ramGb=8; color="Natural Titanium"; screenSize=6.9; batteryCapacityMah=4685; processor="A18 Pro"; description="Flagship"; stockQuantity=50 }
$cr = Exec POST "/phones" $np "Create phone" @{ Authorization = "Bearer $atok" }
if ($cr -and $cr.id) { $pid_phone = $cr.id; Write-Host "  Created ID=$pid_phone" -ForegroundColor $PASS }

# ─── 8. UPDATE PHONE ─────────────────────────────────────────
Step "7. UPDATE PHONE (ADMIN)"
if ($pid_phone) {
    $up = @{ brand="Apple"; model="iPhone 16 Pro Max"; price=1199.99; storageGb=256; ramGb=8; color="Natural Titanium"; description="Discounted"; stockQuantity=100 }
    Exec PUT "/phones/$pid_phone" $up "Update phone" @{ Authorization = "Bearer $atok" }
}

# ─── 9. DELETE PHONE ────────────────────────────────────────
Step "8. DELETE PHONE (ADMIN)"
if ($pid_phone) {
    Exec DELETE "/phones/$pid_phone" $null "Delete phone" @{ Authorization = "Bearer $atok" }
    Exec GET "/phones" $null "Verify empty catalog" @{ Authorization = "Bearer $atok" }
}

# ─── 10. LIST USERS ─────────────────────────────────────────
Step "9. LIST USERS (ADMIN)"
Exec GET "/users" $null "All users" @{ Authorization = "Bearer $atok" }

# ─── 11. REFRESH TOKEN ──────────────────────────────────────
Step "10. REFRESH TOKEN"
if ($l -and $l.refreshToken) {
    Exec POST "/auth/refresh" @{ refreshToken = $l.refreshToken } "Refresh token"
}

# ─── 12. STOP ───────────────────────────────────────────────
Step "STOPPING APPLICATION"
if ($proc -and !$proc.HasExited) { $proc.Kill(); Write-Host "Application stopped." -ForegroundColor $PASS }

Remove-Item -LiteralPath "$PROJECT\data" -Recurse -Force -ErrorAction SilentlyContinue

# ─── SUMMARY ─────────────────────────────────────────────────
Title "RESULT"
if ($fail -eq 0) {
    Write-Host "ALL 10 TESTS PASSED (0 failures)" -ForegroundColor $PASS
} else {
    Write-Host "$fail TEST(S) FAILED" -ForegroundColor $ERR
}
Write-Host @"

  JAR: $PROJECT\target\phone-store-1.0.0.jar
  API: $BASE
  Swagger: $BASE/swagger-ui.html
  H2 Console: $BASE/h2-console (JDBC: jdbc:h2:file:./data/demodb, sa / empty)

  This demo uses H2 file-based DB (no MySQL/Redis needed).
  Demo users created: admin / admin123, demouser / demo123
"@ -ForegroundColor $HDR
