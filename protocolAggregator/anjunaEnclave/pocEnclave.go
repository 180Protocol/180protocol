package main

import (
    "fmt"
    "io/ioutil"
    "log"
    "os"
)

func main() {

    //reading data from provider's encrypted file.

    data, err := ioutil.ReadFile("/home/azureuser/anjuna-poc-host/greetings.txt.sealed")
    if err != nil {
        fmt.Println("File reading error", err)
        return
    }
    fmt.Println("Contents of file:", string(data))
    

    

    //writing reply to provider

    f, err := os.Create("/home/azureuser/anjuna-poc-host/greetings_reply.txt.sealed")

    if err != nil {
        log.Fatal(err)
    }

    defer f.Close()

    _, err2 := f.WriteString("Thank you for providing greetings. \n")

    if err2 != nil {
        log.Fatal(err2)
    }

    fmt.Println("done")
}



