# SourceJump

## Usage

Select some text in the editor and press <kbd>Ctrl</kbd>+<kbd>'</kbd> to
fetch similar results to the highlighted text sorted by contextual
similarity.

![sourcejump](https://user-images.githubusercontent.com/175716/114292084-27c6b480-9a5a-11eb-9d7a-92af43211e03.png)

SourceJump will search GitHub for matching substrings, and display the
results, sorted by semantic similarity to the cursor context.

## Configure

SourceJump will need you to provide a
[Github Personal Access token](https://github.com/settings/tokens).
Generate [a new one](https://github.com/settings/tokens/new) and paste
the token into `Settings | Tools | SourceJump | GitHub token`.

## Building

To build the plugin, run the following command from the parent directory:
```
./gradlew buildPlugin
```