import auth from "./auth/reducer";
import data from "./data/reducer";
import {combineReducers} from "./../utils/helpers"

const reducers = combineReducers({
    auth,
    data
});

export default reducers;
