import auth from "./auth/reducer";
import {combineReducers} from "./../utils/helpers"

const reducers = combineReducers({
    auth
});

export default reducers;
