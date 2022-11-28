// IPrinterService.aidl
package com.example.printertest;

// Declare any non-default types here with import statements

interface IPrinterService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

     /*
         returns error code or 0 if no error
         possible errorcodes are listed in PrinterErrorCode enumeration
     */
    int printerStatus();

    // cursor position should be a multiple of 8
    int cursorPosition(); // not implemented yet
    void setCursorPosition(int pos);

    // for available font and fontsize options, see PrinterDefinitions file
    void setFontFace(int font);
    void setFontSize(int fontSize);

     /*
            Function addTextToLine
            Desc:   Draws given string into line buffer, does not print bitmap automatiacally.
            Useful for creating lines that has different fonts and sizes within
            If alignment is left, text is added starting from current cursor position
            If alignment is right or center, text is added accordingly, cursor position is ignored
    */
    void addTextToLine(String text, int alignment);


     /*
            Function PrintLine
            Desc: Prints line buffer
    */
    void printLine();

    /*
        Function drawLine
        Desc            :   Draws and prints a horizontal line with given thickness and margins
        thickness       :   Vertical thickness of the line in pixels
        verticalMargin  :   The empty space that will be left before and after line, in pixels
        horizontalMargin:   The horizontal space (on left and right) to be left on the edges of the line
                            Horizontal margin should be a multiple of 8 or it will be rounded down
    */
    void drawLine(int thickness, int verticalMargin, int horizontalMargin);

    /*
        Function printText
        Desc: Prints given text, it can be multiline
    */
    void printText(String text);

    /*
        Function printBitmap
        Desc: Prints a preloaded monochrome bitmap file
        verticalMargin  :   The empty space that will be left before and after bmp ppicture, in pixels
        name: name of the preloaded monochrome bitmap file without .bmp extension
    */

    void printBitmap(String name, int verticalMargin);

    /*
        Function addSpace
        Leaves a blank space of given height in pixels. Takes effect immediately, not a buffered command.
    */
    void addSpace(int pixelHeight);

    /*
        Function addEmptyLines
        desc: Leaves a blank space of given height in lineHeights (1.5 lines, for example).  Takes effect immediately, not a buffered command
    */
    void addEmptyLines(float lines);


    /*
        Function (set)LineSpacing
        Desc:   Get or Set Line Spacing
                If linespacing is 1, lines are spaced tightly,
                If linespacing is 1.5 there will be a half line sized gap between them etc.
                Line spacing cannot be less than 1

    */
    float lineSpacing();
    void setLineSpacing(float f);

    /*
            Function (set)printDensity
            Desc:   Get or Set Print Density
                    It can take/return values from 60 to 140. If argument is not in this range, no change is made.

    */
    int printDensity();
    void setPrintDensity(int d);
}
