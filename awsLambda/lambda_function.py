from linebot import (
    LineBotApi, WebhookHandler
)
from linebot.exceptions import (
    InvalidSignatureError
)
from linebot.models import (
    MessageEvent, TextMessage, TextSendMessage,
)

import requests
import os
import json

line_bot_api = LineBotApi('hRdNtgIpsXPzVUL8oDyeZH8YeMrw9R0Y2M0APa4MIsfV7KjpIWi7VvMNhDx8QwEPGY504/XNZU7rKK9oozeCfIkTiizFXLJfk5vevcXE1TeXrAk37e758tVCxFZ+89YCB1w9dx/u9ZUdKH1odgo1vQdB04t89/1O/w1cDnyilFU=')
handler = WebhookHandler('d24cd9e3569335cc868cb782263aef4c')
access_token = 'VfliTL4LOifStYXYxG492kVWBVDtFrcHYbANv8enunm'

messages = ['社辦開了- 19:11', '社辦沒人了-']

url = 'https://notify-api.line.me/api/notify'
headers = {'Authorization': f'Bearer {access_token}'}
def lambda_handler(event, context):
    try:
        print(event['events'][0]['type'])
        print("User: " + event['events'][0]['source']['userId'])
        print(event)
        if event['events'][0]['type'] == 'beacon':
            data = {'message': messages[0]}
            #print(messages[0])
            response = requests.post(url, headers=headers, data=data)
            #print(event)
        #    line_bot_api.reply_message(event['events'][0]['replyToken'], TextSendMessage(text=event['events'][0]['message']['text']))
    except InvalidSignatureError:
        return {
            'statusCode': 502,
            'body': json.dumps("Invalid signature. Please check your channel access token/channel secret.")
            }
    return {
        'statusCode': 200,
        'body': json.dumps("Hello from Lambda!")
        }
        
