[Original NewPipe Readme](../README.md)

## About this fork
Due to restrictive project policy, the NewPipeTeam refuses to add platforms that they
find offensive. This fork (BravePipe) will not be as restrictive. As long as the
platforms work in the spirit of free speech, they could be integrated.

Nevertheless, platforms that promote pornography or other degrading things will
__NOT__ be included here.

## APK variants
Currently there are 3 variants:
- `BravePipe_v${TAG}.apk`: version most people want to install.
- `BravePipe_conscrypt_v${TAG}.apk`: like the first but with lastest TLS library aka conscrypt
- `BravePipe_legacy_v${TAG}.apk`: based on same code like above variants but with some checks to
  make it work on SDK 19 aka Kitkat. The separate repository [BraveNewPipeLegacy](https://github.com/bravepipeproject/BraveNewPipeLegacy)
  is dumped instead. I hope this approach is more reliable and less a burden to maintain.

### APK Info
This is the SHA fingerprint of BravePipe's signing key to verify downloaded APKs.
```
2F:0C:31:D0:7F:70:14:16:B2:94:33:76:49:1C:B1:6E:BB:71:81:56:DE:FC:2B:12:69:AA:C0:4B:94:39:6C:85
```

## Contribute
This fork will focus only on integrating other platforms. Unrelated patches will
be rejected for now.

Feel free to suggest which alternative platforms should be included. Any contribution
(development/testing/bug report) is greatly appreciated.

## Which additional platforms are supported?
- Bitchute
- Rumble

## Other features not found in NewPipe
- merged NewPipe x Sponsorblock into this fork. [NewPipe x Sponsorblock Readme](../README.md)
- searchfilters: in the action menu of the search page you can now change
  the search behavior for the actual search. The supported content/sort
  filters depend on the service. More information [PR TeamNewPipe#8837](https://github.com/TeamNewPipe/NewPipe/pull/8837)
- Integrated updater feature. BravePipe will download a new version and act as an installer
  source (you have to allow that). To enable that feature go to:
  `Settings -> 'BravePipe Settings' -> 'Update Behaviour'`
- option to only scroll comments with fixed video player
- Debug: Option to collect the Logcat output and share it as a logfile
- find more in `Settings -> 'BravePipe Settings'`

## Reporting bugs
Most problems with BravePipe should be reported to the BravePipeExtractor
project, as platform support is developed there.
[Issues](../../../../BravePipeExtractor/issues)

## Building the project
Before building any flavor you should (if you want everything to be named BravePipe) call the
gradle task:
```
gradle bravify
```
For the `brave` and `braveConscrypt` flavor there is nothing special you have to do. But for the
flavor `braveLegacy` you should also call before building:
```
gradle prepareLegacyFlavor
```
It will move some duplicated files from 'main' to a temp directory and alters the new version URL.
Later if you want to restore the files after the Legacy flavor build (in case you want to build
another flavor) run:
```
gradle unPrepareLegacyFlavor
```
