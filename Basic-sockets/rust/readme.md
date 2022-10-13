A distributed LAN chat application in the terminal
Run the application in your terminal and write into the LAN!

Built on top of [tui-rs] to create the terminal UI and
[message-io] to make the network connections.


# Installation

You can use the [cargo][cargo] package manager in order to install it.

```
$ cargo install cartoonchat --all-features
```

If you have `~/.cargo/bin` in your PATH (or similar in your OS), you will be able to use _cartoonchat_ everywhere in your computer!

Also, you can download the last release for your machine from the [releases](https://github.com/anzal1).

[cargo]: https://doc.rust-lang.org/cargo/getting-started/installation.html

# How it works?

To not saturate the network, _cartoonchat_ uses only one multicast message at startup to find other _cartoonchat_ applications on the network.
Once a new application has been found by multicast, a TCP connection is created between them.

## Usage

Simply go to the folder where you have cloned the repository and run the following command:

```
$ cargo run
```

to open the application in your terminal.

By default, your computer user name is used. You can use a different username with `-u <name>`

You can modify the multicast discovery address with `-d <address>`

You can set a custom tcp sever port with `-t <port>`

(see the application help for more info `--help`).

### Commands

cartoonchat treats messages containings the following commands in a special way:

- **`?send <$path_to_file>`**: sends the specified file to everyone on the network,
  example: `?send ./myfile`

  Note: The received files can be found in `/tmp/cartoonchat/<cartoonchat-username>/<file_name>`

- **`?startstream`**/**`?stopstream`**: starts/stops video stream and send it to all peers. Currently this is only supported on linux, the other platforms can only receive the video.

### Config

cartoonchat store its configuration in a simple file located at `$ConfigDir/cartoonchat/config`

**Default config:**

```

discovery_addr = "238.255.0.1:5877"
tcp_server_port = 0
user_name = "my_awesome_user_name"
terminal_bell = true

[theme]
message_colors = ["Blue", "Yellow", "Cyan", "Magenta"]
my_user_color = "Green"
date_color = "DarkGray"
system_info_color = ["Cyan", "LightCyan"]
system_warning_color = ["Yellow", "LightYellow"]
system_error_color = ["Red", "LightRed"]
chat_panel_color = "White"
progress_bar_color = "LightGreen"
command_color = "LightYellow"
input_panel_color = "White"

```

