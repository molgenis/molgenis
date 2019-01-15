**
Molgenis allows for many customizations. One of them is uploading your own bootstrap themes.
**

# Bootstrap themes

Molgenis uses currently two versions of bootstrap: bootstrap 3.3.7 and bootstrap 4.x. To customize 
your complete molgenis, you need a customized theme for both versions of bootstrap. 

## Bootstrap 3
Creating bootstrap 3 themes can be done using the following tool:
[bootstrap-life-customizer](https://www.bootstrap-live-customizer.com/). 

### Fonts
To use different fonts, you need to import them in the theme.less config. You can edit it by
clicking the "Edit theme.less"-button. Here you can import fonts via URL in this way:
```css
@import url("//fonts.googleapis.com/css?family=IBM+Plex+Sans");
```
After doing this you can use the font throughout your bootstrap theme. Selecting the font as base
font can be done in the "Typography" tab of your theme builder.

## Bootstrap 4
Creating bootstrap 4 themes can be done using this tool:
[bootstrap.build](https://bootstrap.build/app).

### Fonts
To use different fonts, they should be imported in the custom.scss. Here you can import fonts like 
this:
```css
@import url("//fonts.googleapis.com/css?family=IBM+Plex+Sans");
```
After doing this you can use the font throughout your bootstrap theme. You can apply your font in 
the "Typography" section of the theme builder.

## Tips and tricks

### Colors
Start with your colors. The colors make the theme. Specify them in the "Colors" tab.
To see which colors a website of your customer uses, open the console of your browser and select the
element with the preferred color. Alternatively, for chrome use the "eye dropper color picker" 
plugin. However please note colors selected by the color picker might be a slightly bit different 
from the original. The color picker is especially handy when selecting colors from images, rather
than HTML elements.

### Exporting
Download at least the unminimized css file. This is the readable version of your css and it can be 
loaded in molgenis using the theme manager. If you created a theme for a specific project, put it in
the molgenis-projects repository on github. It could also be nice to export the .less files for
bootstrap 3 and the .scss files for bootstrap 4. Using these files you can load your theme again in
the theme builder to continue later. 

### Making bootstrap 3 and 4 themes look the same
There is not a real trick for this. Throughout the application the classes between bootstrap 3 and 4
are not always consistent. There are however always some steps that need to be done to accomplish
this.  
#### 1. Determine which font-size you want to use. 
Bootstrap 3 uses 14px as basic font size whereas bootstrap 4 uses 16 px. To change the default of
bootstrap 3 to 16px, set the ```@font-size-base``` to 16px in the "Typography" section. To change the
default of bootstrap 4 to 14px, set the ```$font-size-base``` of the "Typography" section to 0.875rem.

#### 2. Set the same colors
Set your own colors, or copy either the colors of bootstrap 3 of bootstrap 4 to both themes in the
"Color" section. Bootstrap 4 has an additional secondary color, which is not in bootstrap 3, here
you can use the info color, or another if you prefer. 

#### 3. The menu
To make the menu look the same, use your browsers debugger to compare the bootstrap 3 and 4 theme
and copy css styling between the two until they look the same. Do the same alterations in your
bootstrap css. Some of the alterations you want to do cannot be done in the theme builders. Paste
this css code well documented on top of you bootstrap theme file. You might want to use the 
```!important``` in some cases, but be careful with that.

#### 4. Don't mind the sign in and sign out button
These buttons have such different classes between the bootstrap 3 and 4 molgenis menu, that you
probably don't want to focus too much on them. 
