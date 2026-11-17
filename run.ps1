<#
.SYNOPSIS
    Builds and runs Pantheon Creator.

.DESCRIPTION
    By default, starts the web app and opens it in your browser.
    With -Cli, runs the text-only version in this window instead.
    Needs Java 17 or newer. Maven is downloaded automatically the first time.

.EXAMPLE
    .\run.ps1
    Starts the web app on http://localhost:8080 and opens the browser.

.EXAMPLE
    .\run.ps1 -Port 9000 -NoBrowser
    Starts the web app on port 9000 without opening the browser.

.EXAMPLE
    .\run.ps1 -Cli -Seed 42 -Generations 40
    Runs the text version with seed 42 for 40 generations.

.NOTES
    If PowerShell refuses to run the script, run it once like this:
        powershell -ExecutionPolicy Bypass -File .\run.ps1
#>
[CmdletBinding()]
param(
    [int]$Port = 8080,
    [switch]$Cli,
    [long]$Seed,
    [int]$Generations,
    [switch]$NoBrowser,
    [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'
Set-Location -Path $PSScriptRoot

function Find-Java {
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
        return (Join-Path $env:JAVA_HOME 'bin\java.exe')
    }
    $command = Get-Command java -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }
    return $null
}

function Get-JavaMajorVersion([string]$java) {
    # java -version writes to stderr, so run it through cmd to capture it safely
    $output = & cmd /c "`"$java`" -version 2>&1" | Out-String
    if ($output -match 'version "(\d+)(\.(\d+))?') {
        $major = [int]$Matches[1]
        if ($major -eq 1 -and $Matches[3]) {
            $major = [int]$Matches[3]   # old style, e.g. 1.8 means Java 8
        }
        return $major
    }
    return 0
}

function Test-PortInUse([int]$port) {
    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $client.Connect('127.0.0.1', $port)
        return $true
    }
    catch {
        return $false
    }
    finally {
        $client.Dispose()
    }
}

# ---------- check Java ----------

$java = Find-Java
if (-not $java) {
    Write-Host 'Java was not found.' -ForegroundColor Red
    Write-Host 'Install Java 17 or newer, for example with:'
    Write-Host '    winget install EclipseAdoptium.Temurin.21.JDK'
    Write-Host 'Then open a new PowerShell window and run .\run.ps1 again.'
    exit 1
}

$javaVersion = Get-JavaMajorVersion $java
if ($javaVersion -lt 17) {
    Write-Host "Pantheon Creator needs Java 17 or newer, but found Java $javaVersion at $java." -ForegroundColor Red
    Write-Host 'Install a newer Java, for example with:'
    Write-Host '    winget install EclipseAdoptium.Temurin.21.JDK'
    Write-Host 'If you already have one, set JAVA_HOME to its folder and open a new PowerShell window.'
    exit 1
}

if (-not $env:JAVA_HOME) {
    # the Maven wrapper finds Java through JAVA_HOME, so point it at the Java we found
    $env:JAVA_HOME = Split-Path (Split-Path $java -Parent) -Parent
}

# ---------- build ----------

$jar = Join-Path $PSScriptRoot 'target\pantheon-creator.jar'

if (-not $SkipBuild -or -not (Test-Path $jar)) {
    Write-Host 'Building Pantheon Creator (the first build downloads Maven and libraries, which takes a few minutes)...'
    & (Join-Path $PSScriptRoot 'mvnw.cmd') -q -B package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host 'The build failed. See the messages above.' -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

# ---------- text version ----------

if ($Cli) {
    $cliArgs = @()
    if ($PSBoundParameters.ContainsKey('Seed')) { $cliArgs += @('s', $Seed) }
    if ($PSBoundParameters.ContainsKey('Generations')) { $cliArgs += @('g', $Generations) }
    & $java -cp (Join-Path $PSScriptRoot 'target\classes') pantheon.PantheonCreator @cliArgs
    exit $LASTEXITCODE
}

# ---------- web version ----------

if (Test-PortInUse $Port) {
    Write-Host "Port $Port is already in use. Is Pantheon Creator already running?" -ForegroundColor Red
    Write-Host "Close it, or pick another port, for example: .\run.ps1 -Port 8081"
    exit 1
}

$url = "http://localhost:$Port/"
if ($PSBoundParameters.ContainsKey('Seed') -or $PSBoundParameters.ContainsKey('Generations')) {
    $query = @()
    if ($PSBoundParameters.ContainsKey('Seed')) { $query += "seed=$Seed" }
    if ($PSBoundParameters.ContainsKey('Generations')) { $query += "generations=$Generations" }
    $url = $url + '?' + ($query -join '&')
}

Write-Host "Starting Pantheon Creator on port $Port..."
$server = Start-Process -FilePath $java -ArgumentList @('-jar', "`"$jar`"", "--server.port=$Port") -NoNewWindow -PassThru

try {
    $started = $false
    $deadline = (Get-Date).AddSeconds(60)
    while ((Get-Date) -lt $deadline -and -not $server.HasExited) {
        if (Test-PortInUse $Port) {
            $started = $true
            break
        }
        Start-Sleep -Milliseconds 500
    }

    if (-not $started) {
        Write-Host 'The server did not start. See the messages above.' -ForegroundColor Red
        exit 1
    }

    Write-Host ''
    Write-Host "Pantheon Creator is running at $url" -ForegroundColor Green
    Write-Host 'Press Ctrl+C to stop it.'
    if (-not $NoBrowser) {
        Start-Process $url
    }

    Wait-Process -Id $server.Id
}
finally {
    if (-not $server.HasExited) {
        Stop-Process -Id $server.Id -Force -ErrorAction SilentlyContinue
    }
}
