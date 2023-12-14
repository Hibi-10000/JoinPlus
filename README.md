# JoinPlus

Bukkit Plugin for Bukkit 1.17.1~  
プレイヤーが初めてログイン・ログイン・ログアウト・キックされたときの  
メッセージをカスタマイズし接続元の国をログイン時に表示します。

## Config
### Messages
#### FirstJoin
プレイヤーがサーバーに初めてログインした時に表示されます  
`enabled`を`false`にすると表示されなくなります  
`messages`のデフォルト : `&b%player_display_name% &7 has joined for the first time!`  

#### Join
プレイヤーがサーバーにログインした時に表示されます  
`enabled`を`false`にすると表示されなくなります  
`messages`のデフォルト :   
`&a--> &e%player_display_name%&a connected from &b%player_country% &r(&b%player_ip%&r)&a.`  

#### Quit
プレイヤーがサーバーからログアウトした時に表示されます  
`enabled`を`false`にすると表示されなくなります  
`messages`のデフォルト : `&c<-- &e%player_display_name%&c disconnected.`

#### Kick
プレイヤーがサーバーからキックされたときに表示されます  
`enabled`を`false`にすると表示されなくなります  
`messages`のデフォルト : `&c<-- &e%player_display_name%&c was kicked%reason%.`

#### Placeholder
`messages`に使用できるプレースホルダーの一覧です

| Placeholder           | 値                    |
|:----------------------|:---------------------|
| %player_name%         | プレイヤーのID             |
| %player_display_name% | プレイヤーのニックネーム等        |
| %player_uuid%         | プレイヤーのUUID           |
| %player_country%      | プレイヤーの接続元の国          |
| %player_ip%           | プレイヤーのIPアドレス         |
| %total_players%       | サーバー内の合計プレイヤー数       |
| %max_players%         | サーバー設定のmax-playersの値 |
| %reason%              | (Kick時のみ)キックされた理由    |

### GeoLite2
#### Download-URL
GeoLite2データベースをダウンロードする時に使用するURLです  
基本的に変更する必要はありません (GeoIP2を使用したい場合は変更してください)  
デフォルト : `https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-Country&license_key={LICENSE_KEY}&suffix=tar.gz`

#### LicenseKey
GeoLite2データベースをダウンロードする時に使用するライセンスキーです  
[MaxMind](https://dev.maxmind.com/geoip/geolite2-free-geolocation-data?lang=en#accessing-geolite2-free-geolocation-data)
でアカウントを作ってライセンスキーを生成して貼り付けてください  
[EssentialsXGeoIP Wiki](https://essentialsx.net/wiki/GeoIP.html)
に詳しく解説されています

#### LastDBUpdate
GeoLite2データベースの最終更新日です **変更しないでください**  
※GeoLite2データベースはデータベース更新日のサーバー起動時に自動でアップデートされます

## Commands

※コマンドを実行するには joinplus.command パーミッションが必要です

| command             | about                       |
|:--------------------|:----------------------------|
| /joinplus help      | /joinplusの使用法か表示されます        |
| /joinplus reload    | config.ymlをリロードします          |
| /joinplus geoupdate | GeoLite2データベースを手動でアップデートします |

## Permissions

| permission       | about               | default |
|:-----------------|:--------------------|:--------|
| joinplus.command | /joinplusコマンドが使用できる | OP      |
