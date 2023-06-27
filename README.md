# ScreenSaver

## How to use:
Open ScreenSaver-1.0.jar with double click, or run this command in command prompt:
```
java -jar ScreenSaver-1.0.jar
```

## How to add custom gifs:
Copy .gif or .webp file to "assets" folder, if it has transparency, add "T" at the start of the file name. 
So if you have a file "RickRoll.gif", you should rename it to "TRickRoll.gif".
Curently this program supports only gif and webp files.

## How to build:
Clone this repository, and also VisualLib repository, and run "mvn clean package" from ScreenSaver project.

## Command line arguments for nerds:
* -width <number> - set window width
* -height <number> - set window height
* -fullWindowedScreen <true/false> - make window be in windowed mode but at full screen
* -fullScreen <true/false> - real full screen mode (transparent background won't work with this)
* -transparentFramebuffer <true/false> - enable/disable transparent background (true by default)
* -debug <true/false> - enable debug mode, you can move around and turn on/off vsync etc
* -speed <number> - change camera speed (1 by default)
* -maxSpriteSize <number> - limit maximum size of a sprite. By default this value is 256. When ScreenSaver loads a gif or webp animation, it stores all frames in one texture, and in order to reduce memory usage, if frame width or height is bigger than this value, it will be resized. So if you have a gif with 100 frames and each frame is 300x300. Each frame will be resized to 256x256, and all frames will be stored in a texture with size 2560x2560.

Example:
```
java -jar ScreenSaver-1.0.jar -width 800 -height 600 -fullWindowedScreen false -fullScreen false -transparentFramebuffer false -debug true -speed 1 -maxSpriteSize 128
```