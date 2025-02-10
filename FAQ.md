### OSX says that DIYLC is damaged and cannot start ###

It is definitely not damaged, do not give up! There's a thing in Mac OS called Gatekeeper that blocks 3rd party apps by default and as a result they appear to be damaged. It's handled very unfortunately, but it is what it is. To fix the issue, you just need to allow 3rd party apps. Different versions of OSX handle this differently and I've seen many different ways to do it, but this should be a good starting point [https://support.apple.com/kb/ph18657?locale=en_US](https://support.apple.com/guide/mac-help/open-a-mac-app-from-an-unknown-developer-mh40616/mac)

If that doesn't work, try running the command in the terminal

`codesign --remove-signature /Applications/DIYLC.app`

and then:

`codesign --force --deep --sign - /Applications/DIYLC.app`

### CPU spikes on multi-monitor machines ###

Try turning "Hardware Acceleration" config off.

### Error on Mac: Can't open file -psn_0_2359872 ###

Try running the command below in the terminal 
`/System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/LaunchServices.framework/Versions/A/Support/lsregister -u /Applications/DIYLC.app`
