import React, { useState } from 'react';
import FileUploader from '../components/FileUploader';
import Result from '../components/Result';
import axios from 'axios';


const QuizPage = () => {
    const [quiz, setQuiz] = useState(null);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(false);

    const handleFileSuccess = async (extractedText) => {
        try {
            setIsLoading(true);
            setError(null);

            // Formatage correct des données pour le backend
            const quizRequest = {
                content: typeof extractedText === 'object' ? extractedText.text : extractedText,
                title: "Quiz à générer",
                createdAt: new Date()
            };

            const response = await axios.post('http://localhost:8080/api/quizzes/generate', quizRequest, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            setQuiz(response.data);
        } catch (err) {
            console.error("Erreur détaillée:", err.response?.data);
            setError(err.response?.data || "Erreur lors de la génération du quiz");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="quiz-page">
            <h1>Générateur de Quiz</h1>
            
            <FileUploader
                onSuccess={handleFileSuccess}
                onError={(msg) => setError(msg)}
                maxSizeMB={10}
                apiBaseUrl="http://localhost:8080"
            />

            {isLoading && (
                <div className="loading">
                    <p>Génération du quiz en cours...</p>
                </div>
            )}

            {error && (
                <div className="error-message">
                    {error}
                </div>
            )}

            {quiz && (
                <div className="quiz-result">
                    <h2>{quiz.title}</h2>
                    <div className="quiz-content">
                        {quiz.content.split('\n').map((line, i) => (
                            <p key={i}>{line}</p>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default QuizPage;