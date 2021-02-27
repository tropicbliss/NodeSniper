# NodeSniper

A Minecraft username sniper made in Java.

## Why is it called NodeSniper, especially since it is made in Java?

https://imgur.com/a/hixbvFt

## Why

Because.

## Alright, jokes aside:

This sniper was made because I just wanted to make a sniper for fun. It is also easy to use, platform-agnostic, and easily extensible. Take note that this sniper doesn't yet support Microsoft account authentication.

## Credits ❤️:

- [MCsniperPY](https://github.com/MCsniperPY/MCsniperPY) for referencing the APIs used.
- [This](https://mojang-api-docs.netlify.app/) Mojang API documentation for the reference.
- [NX's NameMC API](https://api.nathan.cx/)

### Dependencies used:

- [SnakeYAML](https://mvnrepository.com/artifact/org.yaml/snakeyaml)
- [Jackson](https://github.com/FasterXML/jackson)

## Setup

### Installation

1. Download OpenJDK 15 [here](https://adoptopenjdk.net/). Choose HotSpot as the JVM (cause that's the one I tested with, no other reason).
2. Download the sniper [here](https://github.com/chronicallyunfunny/NodeSniper/releases/tag/v1.0).
3. Unzip the files and move it to an accessible directory in your computer.

### Setting up an account

Open the file "account.yml" with a Notepad application and enter your username and password of your account. Enter the three security questions of your account in the fields "sq1", "sq2", and "sq3". Take note that order matters.

### Setting up the delay

Open "config.yml" and enter the delay you want to use in milliseconds. For more info on delays click [here](https://github.com/MCsniperPY/MCsniperPY#delays). Take note that the delay you use for MCsniperPY and this sniper is different. I honestly don't have enough accounts to test for delays so use whatever delay you like.

## Usage

Navigate to the directory in which the sniper, account and config files are from your terminal. Again if you are not sure MCsniperPY documents a similar process right [here](https://github.com/MCsniperPY/MCsniperPY#installing-dependencies). After that type in the following:

```
java -jar NodeSniper-1.0.jar
```
