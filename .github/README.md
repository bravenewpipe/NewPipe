[Original NewPipe Readme](../README.md)

## About this fork
Due to restrictive project policy, the NewPipeTeam refuses to add platforms that they
find offensive. This fork (BraveNewPipe) will not be as restrictive. As long as the
platforms work in the spirit of free speech, they could be integrated.

Nevertheless, platforms that promote pornography or other degrading things will
__NOT__ be included here.

## APK variants
Currently there are 3 variants:
- `BraveNewPipe_v${TAG}.apk`: version most people want to install.
- `BraveNewPipe_conscrypt_v${TAG}.apk`: like the first but with lastest TLS library aka conscrypt
- `BraveNewPipe_legacy_v${TAG}.apk`: based on same code like above variants but with some checks to
  make it work on SDK 19 aka Kitkat. [BraveNewPipeLegacy](https://github.com/bravenewpipe/BraveNewPipeLegacy)
  is dumped instead. I hope this approach is more reliable and less a burden to maintain.

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
- Integrated updater feature. BraveNewPipe will download a new version and act as an installer
  source (you have to allow that). To enable that feature go to:
  `Settings -> 'BraveNewPipe Settings' -> 'Update Behaviour'`

## Reporting bugs
Most problems with BraveNewPipe should be reported to the NewPipeExtractor
project, as platform support is developed there.
[Issues](../../../../NewPipeExtractor/issues)

## Building the project
Before building any flavor you should (if you want everything to be named BraveNewPipe) call the
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
