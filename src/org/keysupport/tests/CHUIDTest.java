package org.keysupport.tests;
/******************************************************************************
 * The following is part of the KeySupport.org PIV API
 * 
 * $Id: CHUIDTest.java 3 2013-07-23 16:00:13Z grandamp@gmail.com $
 *
 * The KeySupport.org PIV API is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the KeySupport.org PIV API.  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * @author Todd E. Johnson (tejohnson@yahoo.com)
 * @version $Revision: 3 $
 * Last changed: $LastChangedDate: 2013-07-23 12:00:13 -0400 (Tue, 23 Jul 2013) $
 *****************************************************************************/

import java.io.File;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.UUID;

import org.keysupport.keystore.KeyStoreManager;
import org.keysupport.nist80073.PIVCard;
import org.keysupport.nist80073.datamodel.CMSSignedDataObject;
import org.keysupport.nist80073.datamodel.FASCN;
import org.keysupport.nist80073.datamodel.PIVCardHolderUniqueID;
import org.keysupport.smartcardio.CardTerminal;
import org.keysupport.util.DataUtil;

/**
 */
public class CHUIDTest {

	/*****************************************************************************
	 * Reference: NIST SP 800-73-3, Part(s) 1 & 2
	 * 
	 * Full debugging: java -Xmx512m -Djava.security.debug=all PIVTest The
	 * memory is so high to support PDVal testing if the cert path is verified.
	 * @param args String[]
	 ****************************************************************************/

	public static void main(String args[]) {
		//Perform the read and validation test
		readAndValidate();
		//Perform the create and validation test
		//createAndValidate();
	}
	
	public static void readAndValidate() {
		/*
		 * This part of the code retrieves a CHUID from a card, 
		 * and validates the digital signature
		 * 
		 * Begin Read & Validate
		 */
		try {
			//Set up our reader/terminal
			CardTerminal terminal = new CardTerminal();
			// Establish a connection with the card
			PIVCard card = terminal.getPIVCard();
			//Print out some information about the card
			System.out.println("Card: " + card);
			System.out.println("Card ATR: "
					+ DataUtil.byteArrayToString(card.getATR().getBytes()));
			// Get the PIV CHUID
			PIVCardHolderUniqueID chuid = card.getCardHolderUniqueID();
			// Disconnect from the card, we'll keep working with the CHUID
			card.disconnect(false);
			//Print the raw CHUID data
			System.out.println("CHUID Raw Bytes: " + DataUtil.byteArrayToString(chuid.getBytes())); 
			//Print out the CHUID Contents
			System.out.println(chuid.toString());
			//Verify the digital signature, and print the result
			System.out.println("Verifying CHUID Signature:");
			CMSSignedDataObject chuidSig = new CMSSignedDataObject(
					chuid.getSignatureBytes(), chuid.getSignatureDataBytes());
			if (chuidSig.verifySignature(false)) {
				System.out.println("Signature Verified!");
			} else {
				System.out.println("Signature Verification Failed!");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		/*
		 * End Read & Validate
		 */
	}
	
	public static void createAndValidate() {
		/*
		 * This part of the code creates a CHUID, 
		 * generates the signature, presents the final CHUID,
		 * then verify's the digital signature
		 * 
		 * Begin Create & Validate
		 */
		try {
			//Setup the raw FASC-N elements
			String ac = "1102"; //NSC
			String sc = "0001";
			String cn = "000001";
			String cs = "1";
			String ici = "1";
			String pi = "0000000001";
			String oc = "1";
			String oi = "1100"; //EOP 
			String poa = "1";
			//Create the FASC-N
			FASCN myFASCN = new FASCN(ac, sc, cn, cs, ici, pi, oc, oi, poa);
			
			//Setup the raw CHUID elements

			//Let's give this CHUID a 2 week validity period
			Calendar expirydate = Calendar.getInstance();
			expirydate.set(Calendar.MILLISECOND, 0);
			expirydate.add(Calendar.HOUR, 24 * 14);
			String expiry = DataUtil.dateToString(expirydate.getTime());
			
			//Create GUID bytes from a random UUID
			UUID uuid = UUID.randomUUID();
			final byte[] guid = DataUtil.uuidToByteArray(uuid); 			

			//Create the CHUID Object containing the FASC-N, GUID, and Expiry Date
			PIVCardHolderUniqueID myCHUID = new PIVCardHolderUniqueID(myFASCN, null, null, null, guid, expiry);
			
			//Print out the CHUID object to show what we have so far
			System.out.println(myCHUID.toString());
			
			/*
			 * Create a Fictitious PIV Content Signer Certificate and Key
			 * For now, we will use one that we have.
			 * 
			 * The following values must be changed to match the keystore 
			 * in your possession
			 * 
			 *  ####################### BEGIN SENSITIVE ####################### 
			 */
			String STORENAME = "";
			String STOREPASS = "";
			String ALIAS = "";
			
			 /* 
			 *  ######################## END SENSITIVE ######################## 
			 */
			KeyStoreManager myKeys = new KeyStoreManager();
			KeyStore mystore = myKeys.getKeyStore(STOREPASS.toCharArray(), new File(STORENAME));
			X509Certificate signer = myKeys.getCertificate(mystore, ALIAS);
			PrivateKey signerpriv = myKeys.getPrivateKey(mystore, ALIAS, STOREPASS.toCharArray());

			//Generate the CHUID Signature
			CMSSignedDataObject myCHUIDSig = new CMSSignedDataObject(myCHUID.getSignatureDataBytes(), signer, signerpriv);
			//Add signature to CHUID
			myCHUID.setSignatureBytes(myCHUIDSig.sign());
			
			//Print and verify the CHUID data and signature
			PIVCardHolderUniqueID chuid = myCHUID;
			//Print out the CHUID Contents
			System.out.println(chuid.toString());
			//Verify the digital signature, and print the result
			System.out.println("Verifying CHUID Signature:");
			CMSSignedDataObject chuidSig = new CMSSignedDataObject(
					chuid.getSignatureBytes(), chuid.getSignatureDataBytes());
			if (chuidSig.verifySignature(false)) {
				System.out.println("Signature Verified!");
			} else {
				System.out.println("Signature Verification Failed!");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		/*
		 * End Create & Validate
		 */
	}

}
