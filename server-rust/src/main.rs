use std::{
 io::{prelude::*, BufReader}, net::{TcpListener, TcpStream}
};
use serde::{Deserialize, Serialize};

// Structure that define the player caracteristics
#[derive(Serialize, Deserialize, Debug, Clone)]
struct Player {
    name: String,
    x: f64,
    z: f64,
    dimension: String,
}
// The big list that store all of the players
static mut PLAYER_LIST: Vec<Player> = Vec::new();

fn main() {
    let listener = TcpListener::bind("127.0.0.1:8118").unwrap();

    for stream in listener.incoming() {
        let stream = stream.unwrap();
        handle_connection(stream);
    }   
}

fn handle_connection(mut stream: TcpStream) {
    let buf_reader = BufReader::new(&mut stream);
    let request: Vec<_> = buf_reader
        .lines()
        .map(|result| result.unwrap())
        .take_while(|line| !line.is_empty())
        .collect();
    if request[0].contains("{clear}"){
        unsafe { PLAYER_LIST = Vec::new() };
        stream.write_all("OK".as_bytes()).unwrap();
        print!("reset!");
        return;
    }else{
        println!("{}",request[0]);
    }
    let parsed_result: Player = serde_json::from_str(&request[0]).unwrap();
    unsafe {
        let mut already_existed = false;
        for i in 0..PLAYER_LIST.len() {
            if PLAYER_LIST[i].name == parsed_result.name {
                PLAYER_LIST[i] = parsed_result.clone();
                already_existed = true;
                break;
            }
        }
        if !already_existed {
            PLAYER_LIST.push(parsed_result);
        }
        //println!("{}", format!("{{\"player_list\":{:?}}}",player_list).replace("Player ", ""));
        // That's ugly but it does the job of formating the response, see template.json for more details
        let response = format!("{{\"player_list\":{:?}}}",PLAYER_LIST).replace("Player ", "");
        stream.write_all(response.as_bytes()).unwrap();
    }
}