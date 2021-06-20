# AttendanceTally
Each semester, Chinese Club must determine which club members have attended the three meetings required to maintain member status. Each meeting's attendance is tracked in Google Sheets located in a Google Drive folder.

Built with Maven, this Java project uses both the Google Drive and Google Sheets API to first get all the file IDs of the Google Sheets from the folder and then individually go into each sheets file and parse the member's names. From there, it stores each member in a HashMap, with their values corresponding to how many meetings they've attended. Afterwards, it tallies which members have attended the minimum requirement and prints their names into a text file.

### Usage
Running the .jar file will create a new text file named "Attendance.txt" in the parent directory containing a list of all the members who've attended at least three meetings.

### Cloning Repository
To run this project locally, you will need to:
1. Log into the Chinese Club Google account on [Google Cloud Platforms](https://console.cloud.google.com/home/dashboard?project=attendancetally&authuser=2).
2. Open the AttendanceTally project -> Expand in upper-left corner -> APIs & Services -> Credentials -> OAuth 2.0 Client IDs -> download JSON.
3. Create a new resources folder in project main folder -> move JSON into folder.
4. Upon first running, it will ask you to log in to the Chinese Club account and allow it to use the APIs. Agree to everything and it will run in your IDE.
