# SubtitleWordFrequency
An application to create Anki Decks out of the most frequent words in foreign media subtitles

## Note
- This application only works with languages which delimit words with whitespace.
- Currently only .srt subtitles can be read
  
![SubFrq_aplha_2](https://github.com/buffman23/SubtitleWordFrequency/assets/25783731/e7da273e-4d3a-4701-8b3e-2512fbad12e0)
*Application with German and English subtitles loaded*
# Table of Contents
1. [Getting Started](#getting-started)
2. [Word Table](#word-table)
3. [Foreign Subtitle Panels](#foreign-subtitle-panel)
4. [Primary Subtitle Panels](#primary-subtitle-panel)
5. [Tools](#tools)
6. [Export to Anki](#export-to-anki)
7. [Future Plans](#future-plans)

## Getting Started
1. Download the latest release by going to [Releases](https://github.com/buffman23/SubtitleWordFrequency/releases) and downloading 'SubtitleWordFreq.zip'
2. Unzip the release to your preferred location
3. Run the SubtitleWordFreq application
4. Go to File -> Open Subtitles
5. In the file browser, select a pair of subtitles (ex: select the subtitles in the 'sample' folder). Click open.
6. The application is now loaded with subtitles.
7. If you have word table data from a previous session, it can be loaded by going to File -> Import -> Table Data (ex: import 'sample/german.wrdtbl')
8. As the user, you will still need to fill in definitions and group certain words (ex: 'run', 'runs', and 'ran' are english words that should be grouped).
9. read the following sections to learn more about each panel.
   
## Word Table
This is a table containing every word in the foreign subtitles.
- Clicking on a row will highlight the first reference in the subtitle panels.
  - Jump between references by using the navigation panel below the table.
- Mark a word as 'hidden' by clicking on the checkbox in the 'Hidden' column or by using the right-click context menu.
  - Toggle visibility of hidden words by clicking the 'Hide Hidden'/'Show Hidden' button below the table.
  - Hide multiple words at once by selecting multiple rows and then using the context menu -> Toggle Hidden menu.
  - The context menu has an extra option called 'Session' which will hide a word but only for the current session. This means hidden status won't be saved when exporting word table data (.wrdtbl).
- All words are set lowercase by default. Words can be capitalized using the right-click context menu.
- The application is not smart enough to group words that are the same but with slight changes (ex: 'run', 'runs', and 'ran'), so the user must be the one to group those words.To create a group select one or more words then open the context menu and select 'Create Group'
  - On the next menu give the group a name (ex: name the group with the unconjugated form). It is okay if a group name is the same as one of its child words.
  - Click Ok and the group will be created.
  - If any of the words had Tags or definitions the group will inherit these.
  - A group word can be expanded/collapsed by double-clicking on the word in the table. Expanding a group shows all its child words.
  - The count of a group is the sum of its children.
- Most of the time you will not be manually making groups. Instead, use the [Group Finder](#group-finder) tool.
- Click on column headers to sort a column. Sorting is disabled on the Tags and Examples columns
    
## Foreign Subtitle Panel

## Primary Subtitle Panel

## Tools

### Group Finder

## Export to Anki
There is future plans to have the application export directly to Anki using [Anki Connect](https://ankiweb.net/shared/info/2055492159), but for now you can export to CSV by going to (File->Export->CSV) and then within Anki, import the CSV file.
You will need to create a notecard template to import the csv data, or you can import the included deck (SubtitleWordFrequency.apkg) once which will create a card template for you called "Example Sentence".

![SWFrqExportCsv](https://github.com/buffman23/SubtitleWordFrequency/assets/25783731/5409f820-ad04-4423-9300-20877bde6873)
<img src="https://github.com/buffman23/SubtitleWordFrequency/assets/25783731/531c08b8-9ab4-4bec-a4f2-04990e97d82a" width="590"/>

## Future Plans
