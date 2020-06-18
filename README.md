# My Encounters

MVP of an Android contact-tracing application made during initial outbreak of COVID-19. It allows users to select contacts from their phone when they meet, and keep an accurate log of their encounters over time. 

During the initial stages of COVID-19, an accurate log of encounters a person had greatly aided the ability to identify potentially infected individuals, and take appropriate measures thereafter.    

Control of data here lies strictly with the user, as data is only stored locally on the device, and users can export the data in JSON format via their desired method. 

Two main views are included: 

1. **Encounters:** a list of encounters/meetings the user had - and includes details such as *name* and *phone number* of the person they met, as well the *date*, *time* and if users allow *GPS location* of where the encounter took place.

2. **Contacts:** a list of contacts from the user's phone, from which they add encounters with individuals

Permissions: 
1) Mandatory: Read Contacts
2) Optional: Access Fine Location
