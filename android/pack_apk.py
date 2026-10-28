"""把 aapt2 生成的 base.apk 与 classes.dex、assets 合并成未签名 APK。

resources.arsc 必须不压缩（Android 11+ 对 targetSdk>=30 的要求）。
"""
import os
import sys
import zipfile

base_apk, dex_path, assets_dir, out_apk = sys.argv[1:5]

with zipfile.ZipFile(base_apk) as src, zipfile.ZipFile(out_apk, "w") as dst:
    for item in src.infolist():
        data = src.read(item.filename)
        if item.filename == "resources.arsc":
            info = zipfile.ZipInfo(item.filename, date_time=item.date_time)
            info.compress_type = zipfile.ZIP_STORED
            dst.writestr(info, data)
        else:
            info = zipfile.ZipInfo(item.filename, date_time=item.date_time)
            info.compress_type = zipfile.ZIP_DEFLATED
            dst.writestr(info, data)

    info = zipfile.ZipInfo("classes.dex", date_time=(2026, 1, 1, 0, 0, 0))
    info.compress_type = zipfile.ZIP_DEFLATED
    with open(dex_path, "rb") as fh:
        dst.writestr(info, fh.read())

    for root, _dirs, files in os.walk(assets_dir):
        for name in files:
            if name == ".DS_Store" or name.startswith("._"):
                continue
            full = os.path.join(root, name)
            rel = os.path.relpath(full, assets_dir).replace(os.sep, "/")
            info = zipfile.ZipInfo("assets/" + rel, date_time=(2026, 1, 1, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            with open(full, "rb") as fh:
                dst.writestr(info, fh.read())

print("packed", out_apk, os.path.getsize(out_apk), "bytes")
