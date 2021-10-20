import React from "react";
import Header from "../containers/navs/Header";

export default (props) => {
    return (
        <>
            <Header/>
            <div className="container">
                {props.children}
            </div>
        </>
    )
}
