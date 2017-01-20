# KSJavaAPI

## Purpose
Java API for reading, and potentially managing, smart cards with applets that conform to NIST SP 800-73 (all versions)  
PIV, PIV-I, CAC

The intent is to provide an API that is lightweight in order to operate on a PC/terminal as well as mobile devices with no external dependencies.

Interfacing with cards on a PC/terminal makes use of javax.smartcardio, and; interfacing with cards from a mobile device (Android) can occur through NFC or a Java-based USB CCID driver.

The long-term goal is to abstract the javax.smartcardio and Android NFC & USB CCID though some form of a "provider" mechanism for cross-platform use.

At this time, the code to interface via Android NFC & USB CCID is not being shared as open source but in progress to be migrated.

## Usage
The code in the following directory of the source provides some general usage examples:

~/src/org/keysupport/tests

-  CAKTest.java - Provides an example of authentication to a terminal using the Card Authentication Key.
-  CertDownload.java - Retrieves all 4 certificates from the card and pretty prints them to the console.
-  CHUIDTest.java - Retrieves the CHUID and validates the digital signature.
-  FASCNTest.java - Encodes a zero filled FASC-N and outputs in Hex to the console.
-  GetCHUIDTimed.java - Similar to CHUIDTest, only timestamps are added, and PDVAL based certificate validation to the Federal Common Policy Root CA occurs.
-  PIVReadTest.java - Example of end user authentication to the card in order to obtain PIN REQUIRED objects from the PIV Application on the card.

API JavaDoc can be viewed via:

-  ~/doc/index.html

## Important Notes
The ASN.1 types, encoding, and decoding do not currently strictly adhere to rules defined in their respective RFCs. If this is desired, then external libraries can be used to suit your needs.

The cryptography performed by this API (I.e., GENERAL/EXTERNAL AUTHENTICATE, signature & key generation, as well as validation) rely mostly on the underlying JCE provider and is NOT FIPS-140 validated.

This API is NOT on any "approved product list", such as the GSA APL. Since it is a free open source implementation, the intent is not for it to be a product, but an educational API. Putting this API through such validation testing would potentially yield substantial costs, for which it has no revenue backing.

## License
GNU GPL v3: http://www.gnu.org/licenses/gpl.html

## Other References
Migration of code from Google Code:  https://code.google.com/p/keysupport-java-api/
