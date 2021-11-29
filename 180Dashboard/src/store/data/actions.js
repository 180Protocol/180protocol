const ROOT_URL = 'http://localhost:9400/node';

export async function upload(dispatch, payload) {
    const requestOptions = {
        method: 'POST',
        body: payload,
    };

    try {
        let response = await fetch(`${ROOT_URL}/uploadNodeAttachment`, requestOptions);
        let data = await response.json();

        if (payload) {
            dispatch({type: 'UPLOAD_SUCCESS', payload: payload});
            return payload;
        }

        dispatch({type: 'UPLOAD_ERROR', error: 'Error'});
        return;
    } catch (error) {
        dispatch({type: 'UPLOAD_ERROR', error: error});
        console.log(error);
    }
}