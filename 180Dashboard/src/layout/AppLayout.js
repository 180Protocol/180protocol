import React from "react";
import Header from "../containers/navs/Header";
import Footer from "../containers/navs/Footer";
import {makeServer} from "./../server"

export default (props) => {
    if (process.env.NODE_ENV === 'development') {
        makeServer()
    }

    return (
        <>
            <Header/>
            {props.children}
            <Footer/>
        </>
    )
}
