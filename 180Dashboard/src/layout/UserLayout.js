import React from "react";
import Header from "../containers/navs/Header";
import Footer from "../containers/navs/Footer";

export default (props) => {
    return (
        <div className='auth-container'>
            <Header/>
            {props.children}
            <Footer/>
        </div>
    )
}
