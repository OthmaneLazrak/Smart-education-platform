import React, { useState } from 'react';
import FileUploader from '../components/FileUploader';
import Result from '../components/Result';
import axios from 'axios';

const ProblemPage = () => {
    const [problem, setProblem] = useState(null);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(false);

    const handleFileSuccess = async (extractedText) => {
        try {
            setIsLoading(true);
            setError(null);

            const problemRequest = {
                content: typeof extractedText === 'object' ? extractedText.text : extractedText,
                title: "Problème à générer",
                createdAt: new Date()
            };

            const response = await axios.post('http://localhost:8080/api/problems/generate', problemRequest, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            setProblem(response.data);
        } catch (err) {
            console.error("Erreur détaillée:", err.response?.data);
            setError(err.response?.data || "Erreur lors de la génération du problème");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="problem-page">
            <h1>Générateur de Problème</h1>
            
            <FileUploader
                onSuccess={handleFileSuccess}
                onError={(msg) => setError(msg)}
                maxSizeMB={10}
                apiBaseUrl="http://localhost:8080"
            />

            {isLoading && (
                <div className="loading">
                    <p>Génération du problème en cours...</p>
                </div>
            )}

            {error && (
                <div className="error-message">
                    {error}
                </div>
            )}

            {problem && (
                <div className="problem-result">
                    <h2>{problem.title}</h2>
                    <div className="problem-content">
                        {problem.content.split('\n').map((line, i) => (
                            <p key={i}>{line}</p>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default ProblemPage;