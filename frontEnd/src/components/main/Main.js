import { useNavigate } from 'react-router';
import styles from './Main.module.css';



function Main() {
    const navigate = useNavigate();

    return (
        <div>
            <div className={styles.introText}>
                원하시는 서비스를 선택하세요.
            </div>
            <br /><br />

            <div className={styles.buttonsRow}>
                <div className={styles.service1Button} onClick={() => {
                    navigate('/service1');
                }}>
                    <div className={styles.mainText}>혜택<br />직접 선택</div>
                    <div className={styles.subText}><br />SELECT BRANDS</div>
                </div>

                <div className={styles.service2Button} onClick={() => {
                    navigate('/service2');
                }}>
                    <div className={styles.mainText}>마이 데이터<br />불러오기</div>
                    <div className={styles.subText}><br />MY DATA</div>
                </div>

                <div className={styles.service3Button} onClick={() => {
                    navigate('/service3');
                }}>
                    <div className={styles.mainText}>주변<br/> 혜택</div>
                    <div className={styles.subText}><br />NEARBY BENEFITS</div>
                </div>

                <div className={styles.listButton} onClick={() => {
                    navigate('/list');
                }}>
                    <div className={styles.mainText}>전체<br />카드 목록<br /></div>
                    <div className={styles.subText}><br />ALL CARDS</div>
                </div>
            </div>
        </div>
    );
}

export default Main;