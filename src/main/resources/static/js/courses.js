document.addEventListener("DOMContentLoaded", () => {
    loadPublicCourses();
});

async function loadPublicCourses() {
    try {
        const data = await apiJson("/courses/getAllCourses");
        const courses = data.content || data;

        console.log("Courses:", courses);

        // Keep this until we connect cards properly
    } catch (error) {
        console.warn("Could not load courses:", error);
    }
}