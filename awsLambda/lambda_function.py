import requests
import os
import json

line_bot_api = LineBotApi('hRdNtgIpsXPzVUL8oDyeZH8YeMrw9R0Y2M0APa4MIsfV7KjpIWi7VvMNhDx8QwEPGY504/XNZU7rKK9oozeCfIkTiizFXLJfk5vevcXE1TeXrAk37e758tVCxFZ+89YCB1w9dx/u9ZUdKH1odgo1vQdB04t89/1O/w1cDnyilFU=')
handler = WebhookHandler('d24cd9e3569335cc868cb782263aef4c')
#test group token
access_token = 'Wodt0yhKGUjePHshxDvq9B3xTY8PY1qX5HD6MyCaYeW'
#main manga club token
#access_token = 'VfliTL4LOifStYXYxG492kVWBVDtFrcHYbANv8enunm'

messages = ['社辦開了- ', '社辦沒人了- ']

url = 'https://notify-api.line.me/api/notify'
headers = {'Authorization': f'Bearer {access_token}'}

state = False
group = set()
def lambda_handler(event, context):
    global state, group
    get_time = str((datetime.utcnow() + timedelta(hours=8)).strftime('%H:%M'))
    
    try:
        if event['event'] == "enter" and event['name'] not in group:
            group.add(event['name'])
        elif event['event'] == "leave" and event['name'] in group:
            group.remove(event['name'])
        
        if state == False and len(group) > 0:
            data = {'message': messages[0] + get_time}
            response = requests.post(url, headers=headers, data=data)
            state = True
        elif state == True and len(group) == 0:
            data = {'message': messages[1] + get_time}
            response = requests.post(url, headers=headers, data=data)
            state = False
            
    except InvalidSignatureError:
        return {
            'statusCode': 502,
            'body': json.dumps("Invalid signature. Please check your channel access token/channel secret.")
            }
    return {
        'statusCode': 200,
        'body': json.dumps("Hello from Lambda!")
        }
        