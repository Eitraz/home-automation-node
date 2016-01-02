package scripts

import com.eitraz.automation.helpers.RawDeviceHelper

def rawDevicesIsActive = { long timeout, RawDeviceHelper... devices ->
    for (RawDeviceHelper device : devices) {
        if (!device.isActive(timeout))
            return false;
    }
    return true;
}

if (sunIsSet) {

}