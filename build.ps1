param(
    [switch]$SkipInstall
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$src  = Join-Path $root 'src'
$cli  = Join-Path $root 'cli'
$h5   = Join-Path $cli 'dist\build\h5'

Write-Output '==> [1/3] 同步源码到构建目录'
$cliSrc = Join-Path $cli 'src'
if (Test-Path $cliSrc) { Remove-Item -LiteralPath $cliSrc -Recurse -Force }
New-Item -ItemType Directory -Force -Path $cliSrc | Out-Null
robocopy $src $cliSrc /E /NFL /NDL /NJH /NJS /R:1 /W:1 | Out-Null
if ($LASTEXITCODE -ge 8) { throw 'sync failed' }

if (-not $SkipInstall -and -not (Test-Path (Join-Path $cli 'node_modules\@vue\cli-service'))) {
    Write-Output '==> 安装构建依赖（首次较慢）'
    Push-Location $cli
    npm install --ignore-scripts --cache (Join-Path $root '.npm-cache')
    Pop-Location
}

Write-Output '==> [2/3] 构建 H5'
Push-Location $cli
$env:NODE_OPTIONS      = '--openssl-legacy-provider'
$env:NODE_ENV          = 'production'
$env:UNI_PLATFORM      = 'h5'
$env:UNI_CLI_CONTEXT   = $cli
$env:UNI_INPUT_DIR     = $cliSrc
npx vue-cli-service uni-build
if ($LASTEXITCODE -ne 0) { Pop-Location; throw 'h5 build failed' }
Pop-Location

if (-not (Test-Path $h5)) { throw "h5 output missing: $h5" }

Write-Output '==> [3/3] 打包 APK'
& (Join-Path $root 'android\build-apk.ps1') -H5Dir $h5 -OutApk (Join-Path $root 'dist\leave-school-1.1.0.apk')
