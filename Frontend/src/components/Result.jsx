import React from 'react';

const Result = ({ data }) => {
    console.log("Données reçues dans Result:", data);

    if (!data) {
        return <p>Aucun résultat à afficher</p>;
    }

    return (
        <div className="quiz-result">
            <h2>{data.title || "Quiz généré"}</h2>
            <pre className="quiz-content">
                {data.content}
            </pre>
        </div>
    );
};




export default Result;