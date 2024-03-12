Lyrics Display App

This is a Java Swing application that allows users to search for song lyrics by entering the song name and artist. It fetches the lyrics from the lrclib.net API and displays them in a dialog window.

Features

   Search for song lyrics by entering the song name and artist.
    Displays the lyrics in a dialog window with scrolling.
    Supports both plain lyrics and synced lyrics.

Requirements

    Java Development Kit (JDK) 8 or higher
    Apache Maven (optional, for building the project)
    

Usage   
Clone the repository to your local machine:


git clone https://github.com/yourusername/lyrics-display-app.git
Compile the Java source files:

    cd lyrics-display-app javac *.java
Run the application:

     java LyricsDisplayApp


  Enter the song name and artist, then click "Display Lyrics" to view the lyrics.

Dependencies

    JSON.simple library: https://github.com/fangyidong/json-simple - Used for parsing JSON responses from the lrclib.net API.

Contributing

Contributions are welcome! If you'd like to contribute to this project, please follow these steps:

    Fork the repository
    Create your feature branch (git checkout -b feature/YourFeature)
    Commit your changes (git commit -am 'Add some feature')
    Push to the branch (git push origin feature/YourFeature)
    Create a new Pull Request

Please ensure your code follows the existing coding style and conventions.

License
NA
