# NodeSniper

<img src="https://img.shields.io/badge/JDK-11-orange"> [![CodeFactor](https://www.codefactor.io/repository/github/chronicallyunfunny/nodesniper/badge/main)](https://www.codefactor.io/repository/github/chronicallyunfunny/nodesniper/overview/main)

A Minecraft username sniper made in Java.

## Features

- Mojang account sniping
- Microsoft account sniping
- GC sniping (package from source if you want this feature)
- Spread (delay between asynchronous sniping requests)
- Auto offset (automatically calculating the offset, also known as delay for sniping, yes you also must package this from source if you want this feature)

## Why is it called NodeSniper, when it is made in Java?

https://imgur.com/a/hixbvFt

## Why

Because.

## Alright, jokes aside:

This sniper was made because I just wanted to make a sniper for fun. It is also easy to use, platform-agnostic, and easily extensible. It supports Microsoft authentication and soon many more cool features. I don't have all the time in the world to maintain this project (I'm a fuckin' student), so I am only going to add important features and then call it a day. I'd like to move on to other projects that doesn't involve Java 🤮.

## Credits ❤️

- [MCsniperPY](https://github.com/MCsniperPY/MCsniperPY) for referencing the APIs used.
- [This](https://mojang-api-docs.netlify.app/) Mojang API documentation for the reference.
- [Kqzz's MC-API](https://kqzz.github.io/MC-API/#/)

### Dependencies used:

- [SnakeYAML](https://mvnrepository.com/artifact/org.yaml/snakeyaml)
- [Jackson](https://github.com/FasterXML/jackson)

## Setup

### Installation

1. Download OpenJDK 11 [here](https://adoptopenjdk.net/). Choose HotSpot as the JVM (cause that's the one I tested with, no other reason).
2. Download the sniper [here](https://github.com/chronicallyunfunny/NodeSniper/releases/download/v2.2/NodeSniper-2.2.zip).
3. Unzip the files and move it to an accessible directory in your computer.
4. Set up Dimension 4 / `chronyd` and shit. If you came from MCsniperPY you should know.

### Setting up an account

Open the file "account.yml" with a Notepad application and enter your username and password of your account. Enter the three security questions of your account in the fields "sq1", "sq2", and "sq3". Take note that order matters. The YAML parser can be quite picky so it will return an exception if it is of the incorrect format.

Important info: A weird quirk of .yml files is after every colon in each field, you gotta put a space, otherwise the program will crash. I regret using YAML.

### Indicating whether you are using a Mojang or Microsoft account

Open "config.yml" and enter true or false under the field "microsoftAuth".

### Setting up the delay

Open "config.yml" and enter the delay you want to use in milliseconds. For more info on delays click [here](https://github.com/MCsniperPY/MCsniperPY#delays). Take note that the delay you use for MCsniperPY and this sniper is different. I honestly don't have enough accounts to test for delays so use whatever delay you like.

### Changing skin after snipe

Move the skin file into the same directory as the JAR file.

### Additional config options

Spread: Determines the amount of milliseconds the sniper waits after sending a request before sending the next. It's asynchronous so you don't have to worry about the receiving time.

The following fields below concern the name change feature of NodeSniper:

changeSkin: Determines whether to change skin after snipe.

skinModel: Choice between "classic" and "slim".

skinFileName: The name of the skin file, with extensions of course.

## Usage

Navigate to the directory in which the sniper, account and config files are from your terminal. Again if you are not sure MCsniperPY documents a similar process right [here](https://github.com/MCsniperPY/MCsniperPY#installing-dependencies). After that type in the following:

```
java -jar NodeSniper-2.2.jar
```

## Packaging JAR file from source

```
mvn package -f "NodeSniper/pom.xml"
```

## Need help?

If you are inexperienced it's probably better to use MCsniperPY instead, especially since it has a large community willing to help you.

## Bug reporting

Feel free to use the GitHub issues tab or DM me @chronicallyunfunny#1113 through MCsniperPY's Discord. This is a new sniper so there may be tons of bugs.
