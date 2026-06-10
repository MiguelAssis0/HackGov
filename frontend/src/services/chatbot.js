const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

export async function requestAI(message) {
    const response = await fetch(`${API_URL}/ai`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${localStorage.getItem("hackgov.accessToken")}`,
      },
      body: JSON.stringify({ message }),
    });
    return response.json();
}