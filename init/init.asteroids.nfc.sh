#!/vendor/bin/sh

pbid=$(getprop ro.boot.pbid)
sku=$(getprop ro.boot.hardware.sku)

if [ "$sku" = "JPN" ]; then
    setprop persist.vendor.nfc.config_file_name "libnfc-hal-st54j-JPN.conf"
    setprop persist.vendor.nfc_model "ST54"
    exit 0
fi

for hw_version in /sys/bus/i2c/devices/*-0008/hw_version; do
    [ -f "$hw_version" ] || continue
    hwid=$(cat "$hw_version")

    case "$hwid" in
        "ST21")
            case "$pbid" in
                "Base")
                    setprop persist.vendor.nfc.config_file_name "libnfc-hal-st21-BASE.conf"
                    ;;
                "Pro")
                    setprop persist.vendor.nfc.config_file_name "libnfc-hal-st21-PRO.conf"
                    ;;
            esac

            setprop persist.vendor.nfc_model "ST21"
            ;;
        "ST54")
            setprop persist.vendor.nfc.config_file_name "libnfc-hal-st54j-PRO.conf"
            setprop persist.vendor.nfc_model "ST54"
            ;;
    esac
    break
done
