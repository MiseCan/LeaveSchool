param(
    [string]$H5Dir,
    [string]$OutApk,
    [string]$SdkDir,
    [string]$JdkDir
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
if (-not $H5Dir) { $H5Dir = Join-Path $root '..\cli\dist\build\h5' }
if (-not $OutApk) { $OutApk = Join-Path $root '..\dist\leave-school-1.1.0.apk' }

if (-not $SdkDir) { $SdkDir = $env:ANDROID_SDK_ROOT }
if (-not $SdkDir) { $SdkDir = Join-Path $env:USERPROFILE 'AppData\Local\Android\Sdk' }
$sdk = $SdkDir
$bt         = Join-Path $sdk 'build-tools\35.0.0'
$androidJar = Join-Path $sdk 'platforms\android-35\android.jar'
if (-not $JdkDir) { $JdkDir = $env:JAVA_HOME }
if (-not $JdkDir) { $JdkDir = Join-Path $env:USERPROFILE '.jdks\jbr-21.0.11' }
$jdk = $JdkDir
$env:JAVA_HOME = $jdk

$aapt2     = Join-Path $bt 'aapt2.exe'
$zipalign  = Join-Path $bt 'zipalign.exe'
$apksigner = Join-Path $bt 'apksigner.bat'
$d8        = Join-Path $bt 'd8.bat'
$javac     = Join-Path $jdk 'bin\javac.exe'
$jar       = Join-Path $jdk 'bin\jar.exe'
$keytool   = Join-Path $jdk 'bin\keytool.exe'

Push-Location $root
try {
$root = '.'
$build     = Join-Path $root 'build'
$assetsWww = Join-Path $root 'assets\www'
$keystore  = Join-Path $root 'leave-school.jks'

if (-not (Test-Path $H5Dir)) { throw "H5 build dir missing: $H5Dir" }

if (Test-Path $build) {
    Remove-Item -LiteralPath $build -Recurse -Force -ErrorAction SilentlyContinue
    if (Test-Path $build) {
        $build = Join-Path $root ("build-" + (Get-Date -Format 'HHmmss'))
        Write-Warning "build 目录被占用，改用 $build"
    }
}
New-Item -ItemType Directory -Force -Path $build, (Join-Path $build 'gen'), (Join-Path $build 'classes'), (Join-Path $build 'dex') | Out-Null

# 1. 把 H5 产物放进 assets/www
if (Test-Path $assetsWww) {
    Remove-Item -LiteralPath $assetsWww -Recurse -Force -ErrorAction SilentlyContinue
}
New-Item -ItemType Directory -Force -Path $assetsWww | Out-Null
Copy-Item -Path (Join-Path $H5Dir '*') -Destination $assetsWww -Recurse -Force

# 2. aapt2 compile + link
& $aapt2 compile --dir (Join-Path $root 'res') -o (Join-Path $build 'res.zip')
if ($LASTEXITCODE -ne 0) { throw 'aapt2 compile failed' }

& $aapt2 link `
    -o (Join-Path $build 'base.apk') `
    -I $androidJar `
    --manifest (Join-Path $root 'AndroidManifest.xml') `
    -R (Join-Path $build 'res.zip') `
    --java (Join-Path $build 'gen') `
    --auto-add-overlay `
    --min-sdk-version 21 `
    --target-sdk-version 33
if ($LASTEXITCODE -ne 0) { throw 'aapt2 link failed' }

# 3. javac
$sources = @()
$sources += Get-ChildItem -Path (Join-Path $build 'gen') -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
$sources += Get-ChildItem -Path (Join-Path $root 'java') -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
& $javac --release 8 -encoding UTF-8 -nowarn -cp $androidJar -d (Join-Path $build 'classes') $sources
if ($LASTEXITCODE -ne 0) { throw 'javac failed' }

# 4. d8
& $jar cf (Join-Path $build 'classes.jar') -C (Join-Path $build 'classes') .
if ($LASTEXITCODE -ne 0) { throw 'jar failed' }
& $d8 --release --min-api 21 --lib $androidJar --output (Join-Path $build 'dex') (Join-Path $build 'classes.jar')
if ($LASTEXITCODE -ne 0) { throw 'd8 failed' }

# 5. pack（resources.arsc 保持不压缩）
& python (Join-Path $root 'pack_apk.py') (Join-Path $build 'base.apk') (Join-Path $build 'dex\classes.dex') (Join-Path $root 'assets') (Join-Path $build 'unaligned.apk')
if ($LASTEXITCODE -ne 0) { throw 'pack failed' }

# 6. zipalign
& $zipalign -f -p 4 (Join-Path $build 'unaligned.apk') (Join-Path $build 'aligned.apk')
if ($LASTEXITCODE -ne 0) { throw 'zipalign failed' }

# 7. 签名
if (-not (Test-Path $keystore)) {
    & $keytool -genkeypair -v -keystore $keystore -alias leaveschool -keyalg RSA -keysize 2048 `
        -validity 10000 -storepass android -keypass android `
        -dname "CN=Leave School, OU=Local Build, O=Local, L=Local, ST=Local, C=CN"
    if ($LASTEXITCODE -ne 0) { throw 'keytool failed' }
}

New-Item -ItemType Directory -Force -Path (Split-Path $OutApk) | Out-Null
& $apksigner sign --ks $keystore --ks-pass pass:android --key-pass pass:android `
    --v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled true `
    --out $OutApk (Join-Path $build 'aligned.apk')
if ($LASTEXITCODE -ne 0) { throw 'apksigner sign failed' }

& $apksigner verify --print-certs $OutApk | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'APK signature verification failed' }
Write-Output "OK -> $OutApk"


} finally { Pop-Location }

