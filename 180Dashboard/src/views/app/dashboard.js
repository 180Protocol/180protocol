import React, {useState, useEffect} from "react";

const user = JSON.parse(localStorage.getItem('user'));
const fetchURL = user ? `http://${user.username}:${user.password}` : 'http:localhost:9400';
const Dashboard = () => {
    const [data, setData] = useState(null)
    const getData = () =>
        fetch(`${fetchURL}/node/info`)
            .then((res) => res.json())

    useEffect(() => {
        getData().then((data) => setData(data))
    }, []);

    return (
        <>
            <h4>Node Info</h4>
            {
                data ? <div>
                    <p>Host : {data.addresses[0].host} </p>
                    <p>Port : {data.addresses[0].port} </p>
                    <p>Party : {data.legalIdentitiesAndCerts[0].party.name} </p>
                </div> : null
            }
        </>
    );
};

export default Dashboard;
