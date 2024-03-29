<!--
    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
-->

# JoinPlus

Bukkit Plugin for Bukkit 1.17.1~  
プレイヤーが初めてログイン・ログイン・ログアウト・キックされたときの  
メッセージをカスタマイズしログイン時に接続元の国を表示します。

## Placeholder
`config.yml`内の`messages.*.message`内で使用できるプレースホルダーの一覧です

| Placeholder           | 値                          |
|:----------------------|:---------------------------|
| %player_name%         | プレイヤーのID                   |
| %player_display_name% | プレイヤーのニックネーム等              |
| %player_uuid%         | プレイヤーのUUID                 |
| %player_country%      | プレイヤーの接続元の国                |
| %player_ip%           | プレイヤーのIPアドレス               |
| %player_ip_masked%    | プレイヤーのIPアドレス (最後の8ビットをマスク) |
| %total_players%       | サーバー内の合計プレイヤー数             |
| %max_players%         | サーバー設定のmax-playersの値       |
| %reason%              | (Kick時のみ)キックされた理由          |

## LicenseKeyについて
GeoIP2データベースを更新する際に使用するライセンスキーです  
`config.yml`内の`GeoIP2.licenseKey`に設定します  
[MaxMind](https://dev.maxmind.com/geoip/geolite2-free-geolocation-data?lang=en#accessing-geolite2-free-geolocation-data) でアカウントを作ってライセンスキーを生成してください  
[EssentialsXGeoIP Wiki](https://essentialsx.net/wiki/GeoIP.html) に詳しく解説されています

## Commands

※コマンドを実行するには joinplus.command パーミッションが必要です

| command             | about                     |
|:--------------------|:--------------------------|
| /joinplus help      | `/joinplus`の使用法か表示されます    |
| /joinplus reload    | `config.yml`をリロードします      |
| /joinplus geoupdate | GeoIP2データベースを手動でアップデートします |

## Permissions

| permission         | about                           | default |
|:-------------------|:--------------------------------|:--------|
| joinplus.command   | `/joinplus`コマンドが使用できる           | OP      |
| joinplus.reload    | `/joinplus reload`コマンドが使用できる    | OP      |
| joinplus.geoupdate | `/joinplus geoupdate`コマンドが使用できる | OP      |
