package main

import (
    "fmt"
    "io/ioutil"
)

func main() {
    data, err := ioutil.ReadFile("/home/azureuser/anjuna-poc-host/greetings.txt.sealed")
    if err != nil {
        fmt.Println("File reading error", err)
        return
    }
    fmt.Println("Contents of file:", string(data))
    //fmt.Println("!... Hello World ...!")
}



