export async function createAggregationRequest(dispatch, apiUrl, payload) {
    const requestOptions = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    };
    try {
        let response = await fetch(`${apiUrl}/node/180 Protocol Broker Flows/ConsumerAggregationFlow`, requestOptions);
        let data = await response.json();

        if (data) {
            dispatch({type: 'DATA_SUCCESS', payload: data});
            return data;
        }

        dispatch({type: 'DATA_ERROR', error: 'Error'});
        return;
    } catch (error) {
        dispatch({type: 'DATA_ERROR', error: error});
        console.log(error);
    }
}

export async function fetchEncryptedDataOutput(dispatch, apiUrl, payload) {
    const requestOptions = {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    };

    let userInfo = JSON.parse(localStorage.getItem('user'));

    try {
        let response = await fetch(`${apiUrl}/node/180Protocol Broker Contracts/DataOutputState/query?participant=${encodeURIComponent(userInfo.name)}`, requestOptions);
        let data = await response.json();
        if (data) {
            dispatch({type: 'FETCH_ENCRYPTED_DATA_OUTPUT_SUCCESS', payload: data});
            return data;
        }

        dispatch({type: 'FETCH_ENCRYPTED_DATA_OUTPUT_ERROR', error: 'Error'});
        return;
    } catch (error) {
        dispatch({type: 'FETCH_ENCRYPTED_DATA_OUTPUT_ERROR', error: error});
        console.log(error);
    }
}

export async function fetchDecryptedDataOutput(dispatch, apiUrl, payload) {
    const requestOptions = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    };

    try {
        let response = await fetch(`${apiUrl}/node/180 Protocol Broker Flows/ConsumerDataOutputRetrievalFlow?wait=1`, requestOptions);
        let data = await response.json();
        let value = data.result.value ? data.result.value.split("\n") : [];
        let result = [];
        for (let i = 0; i < value.length; i++) {
            let parsedData = JSON.parse(value[i]);
            for (const [key, value] of Object.entries(parsedData)) {
                if (value instanceof Object) {
                    parsedData[key] = typeof Object.values(value)[0] === "number" ? Intl.NumberFormat().format(Object.values(value)[0]) : Object.values(value)[0];
                } else {
                    parsedData[key] = typeof value === "number" ? Intl.NumberFormat().format(value) : value;
                }
            }

            result.push(parsedData);
        }

        if (result) {
            dispatch({type: 'FETCH_DECRYPTED_DATA_OUTPUT_SUCCESS', payload: result});
            return result;
        }

        dispatch({type: 'FETCH_ENCRYPTED_DATA_OUTPUT_ERROR', error: 'Error'});
        return;
    } catch (error) {
        dispatch({type: 'FETCH_DECRYPTED_DATA_OUTPUT_SUCCESS', error: error});
        console.log(error);
    }
}