const courseResponseDtoToCourse = (dto) => {
    return{
        id: dto.id,
        title: dto.title,
        color: dto.color
    };
};

export const createCourse = async(req) => {
    const res = await fetch("/api/v1/courses", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(req)
    });

    if(!res.ok) {
        let message;
        try {
            const errorRes = await res.json();
            message = errorRes.error;
        } catch {
            message = `Request error, is backend running?: ${res.status} ${res.statusText}`
        }
        throw new Error(message);
    }

    return courseResponseDtoToCourse(await res.json());
};

export const listCourses = async() => {
    const res = await fetch("/api/v1/courses", {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        }
    });

    if(!res.ok) {
        let message;
        try {
            const errorRes = await res.json();
            message = errorRes.error;
        } catch {
            message = `Request error, is backend running?: ${res.status} ${res.statusText}`
        }
        throw new Error(message);
    }
    const dtos = await res.json();
    return dtos.map((dto) => courseResponseDtoToCourse(dto));
};