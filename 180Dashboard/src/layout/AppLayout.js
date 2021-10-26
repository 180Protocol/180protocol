import React from "react";
import Header from "../containers/navs/Header";
import Footer from "../containers/navs/Footer";

export default (props) => {
    return (
        <>
            <Header/>
            {props.children}
            <Footer/>
        </>
    )
}
