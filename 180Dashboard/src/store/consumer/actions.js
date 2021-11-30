import {API_URL, DECRYPTED_DATA_OUTPUT_RESPONSE, ENCRYPTED_DATA_OUTPUT_RESPONSE} from "../../utils/constants";

export async function createAggregationRequest(dispatch, payload) {
    const requestOptions = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    };

    try {
        let response = await fetch(`${API_URL}/uploadNodeAttachment`, requestOptions);
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

export async function fetchEncryptedDataOutput(dispatch, payload) {
    // const requestOptions = {
    //     method: 'GET',
    //     headers: {
    //         'Content-Type': 'application/json'
    //     }
    // };

    try {
        // let response = await fetch(`${API_URL}/uploadNodeAttachment`, requestOptions);
        // let data = await response.json();
        //
        // if (data) {
        dispatch({type: 'FETCH_ENCRYPTED_DATA_OUTPUT_SUCCESS', payload: ENCRYPTED_DATA_OUTPUT_RESPONSE});
        return ENCRYPTED_DATA_OUTPUT_RESPONSE;
        // }
        //
        // dispatch({type: 'FETCH_ENCRYPTED_DATA_OUTPUT_ERROR', error: 'Error'});
        // return;
    } catch (error) {
        dispatch({type: 'FETCH_ENCRYPTED_DATA_OUTPUT_ERROR', error: error});
        console.log(error);
    }
}

export async function fetchDecryptedDataOutput(dispatch, payload) {
    // const requestOptions = {
    //     method: 'GET',
    //     headers: {
    //         'Content-Type': 'application/json'
    //     }
    // };

    try {
        // let response = await fetch(`${API_URL}/uploadNodeAttachment`, requestOptions);
        // let data = await response.json();
        //
        // if (data) {
        dispatch({type: 'FETCH_DECRYPTED_DATA_OUTPUT_SUCCESS', payload: DECRYPTED_DATA_OUTPUT_RESPONSE});
        return DECRYPTED_DATA_OUTPUT_RESPONSE;
        // }
        //
        // dispatch({type: 'FETCH_ENCRYPTED_DATA_OUTPUT_ERROR', error: 'Error'});
        // return;
    } catch (error) {
        dispatch({type: 'FETCH_DECRYPTED_DATA_OUTPUT_SUCCESS', error: error});
        console.log(error);
    }
}