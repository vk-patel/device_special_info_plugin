#import "DeviceSpecialInfoPlugin.h"
#if __has_include(<device_special_info/device_special_info-Swift.h>)
#import <device_special_info/device_special_info-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "device_special_info-Swift.h"
#endif

@implementation DeviceSpecialInfoPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftDeviceSpecialInfoPlugin registerWithRegistrar:registrar];
}
@end
