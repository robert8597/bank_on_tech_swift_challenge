import base64
import os
import json
import unittest
import Beginner
import xml.etree.ElementTree as ET


class TestBeginner(unittest.TestCase):

    def test_post_interact_message(self):
        with open(os.path.join(os.path.dirname(__file__), 'responses/response_post_interact_message.json'), 'r') as f:
            response_interact_message_post_json = json.load(f)

        message_post_response, msg = Beginner.post_interact_message()

        self.assertEqual(
            message_post_response.json(),
            response_interact_message_post_json
        )

        self.assertEqual(
            message_post_response.status_code,
            201
        )

        self.assertEqual(msg.get_sender_reference(), "BankOnTech", "Sender reference is wrong")
        self.assertEqual(msg.get_service_code(), "swift.finplus!pc", "Service code is wrong")
        self.assertEqual(msg.get_message_type(), "pacs.008.001.13", "Message type is wrong")
        self.assertEqual(msg.get_requestor(), "ou=xxx,o=deutdeff,o=swift", "Requestor is wrong")
        self.assertEqual(msg.get_responder(), "ou=xxx,o=bktrus33,o=swift", "Responder is wrong")
        self.assertEqual(msg.get_format(), "MX", "Format is wrong")

        xml_content = base64.b64decode(msg.get_payload()).decode("utf-8")
        root = ET.fromstring(xml_content)

        ns = {'ns': 'urn:iso:std:iso:20022:tech:xsd:pacs.008.001.13'}
        
        amt = root.find('.//ns:IntrBkSttlmAmt', ns)
        self.assertIsNotNone(amt, "IntrBkSttlmAmt is missing in XML")
        self.assertEqual(amt.text, '100.00', f"IntrBkSttlmAmt value is incorrect: {amt.text}")
        self.assertEqual(amt.attrib.get('Ccy'), 'EUR', f"IntrBkSttlmAmt currency is incorrect: {amt.attrib.get('Ccy')}")
        
        agrd_rate = root.find('.//ns:AgrdRate', ns)
        self.assertIsNotNone(agrd_rate, "AgrdRate is missing in XML")
        self.assertEqual(agrd_rate.find('ns:UnitCcy', ns).text, 'EUR', "UnitCcy is incorrect or missing")
        self.assertEqual(agrd_rate.find('ns:QtdCcy', ns).text, 'USD', "QtdCcy is incorrect or missing")
        self.assertEqual(agrd_rate.find('ns:PreAgrdXchgRate', ns).text, '1.17246', "PreAgrdXchgRate is incorrect or missing")
        
        dbtr = root.find('.//ns:Dbtr', ns)
        self.assertIsNotNone(dbtr, "Dbtr is missing in XML")
        self.assertEqual(dbtr.find('ns:Nm', ns).text, 'Max Mustermann', "Dbtr name is incorrect or missing")
        
        dbtr_agt = root.find('.//ns:DbtrAgt/ns:FinInstnId', ns)
        self.assertIsNotNone(dbtr_agt, "DbtrAgt/FinInstnId is missing in XML")
        self.assertEqual(dbtr_agt.find('ns:BICFI', ns).text, 'DEUTDEFFXXX', "DbtrAgt BICFI is incorrect or missing")
        self.assertEqual(dbtr_agt.find('ns:Nm', ns).text, 'DEUTSCHE BANK AG', "DbtrAgt name is incorrect or missing")

        cdtr_agt = root.find('.//ns:CdtrAgt/ns:FinInstnId', ns)
        self.assertIsNotNone(cdtr_agt, "CdtrAgt/FinInstnId is missing in XML")
        self.assertEqual(cdtr_agt.find('ns:BICFI', ns).text, 'BKTRUS33XXX',
                         "CdtrAgt BICFI is incorrect or missing")
        self.assertEqual(cdtr_agt.find('ns:Nm', ns).text, 'DEUTSCHE BANK TRUST COMPANY AMERICAS',
                         "CdtrAgt name is incorrect or missing")

        cdtr = root.find('.//ns:Cdtr', ns)
        self.assertIsNotNone(cdtr, "Cdtr is missing in XML")
        self.assertEqual(cdtr.find('ns:Nm', ns).text, 'Jane Doe', "Cdtr name is incorrect or missing")


    def test_get_distributions(self):
        with open(os.path.join(os.path.dirname(__file__), 'responses/response_distributions_list.json'), 'r') as f:
            response_list_of_distributions_json = json.load(f)

        get_distributions_response = Beginner.get_distributions()

        self.assertEqual(
            get_distributions_response.json(),
            response_list_of_distributions_json
        )

        self.assertEqual(
            get_distributions_response.status_code,
            200
        )

    def test_get_interact_message(self):
        with open(os.path.join(os.path.dirname(__file__), 'responses/response_get_interact_message.json'), 'r') as f:
            response_get_interact_message_json = json.load(f)

        get_interact_message_response, url = Beginner.get_interact_message()

        self.assertIn("44984189499", url)

        self.assertEqual(
            get_interact_message_response.json(),
            response_get_interact_message_json
        )

        self.assertEqual(
            get_interact_message_response.status_code,
            200
        )

    def test_post_ack(self):
        post_ack_response, url = Beginner.post_ack()

        self.assertIn("44984189499", url)

        self.assertEqual(
            post_ack_response.status_code,
            204,
            "Acknowledgement posting failed"
        )


if __name__ == "__main_":
    unittest.main()
