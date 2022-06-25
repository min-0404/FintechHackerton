import styles from '../Service.module.css';

import { useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';


function Service2() {
    const [mydata, setMydatas] = useState({
        bank_name: "",
        res_list: [{}],
    });

    const navigate = useNavigate();

    useEffect(() => {
        fetch("/serviceTwo/save")
            .then((response) => {
                return response.json();
            })
            .then(data => {
                setMydatas(data);
            });
    }, []);

    return (
        <div>
            {mydata?.bank_name}
            <button onClick={() => {
                navigate('/service2/results')
            }}>
                추천받기
            </button>
        </div >
    );
}

export default Service2;