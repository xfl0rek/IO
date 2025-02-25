import { useForm } from "react-hook-form";
import { useNavigate, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import api from "../../../api/Axios.tsx";
import {useTranslation} from "react-i18next";

export interface Resource {
    resourceId?: number;
    resourceName: string;
    resourceType: string;
    resourceQuantity: number;
    resourceStatus: string;
    warehouseId: number;
}

const ResourceForm = () => {
    const [warehouses, setWarehouses] = useState<{ warehouseId: number; warehouseName: string; location: string }[]>([]);
    const { t } = useTranslation();
    const form = useForm<Resource>({
        defaultValues: {
            resourceName: "",
            resourceType: "",
            resourceQuantity: 0,
            resourceStatus: "PENDING",
            warehouseId: 1,
        },
    });

    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        const fetchWarehouses = async () => {
            try {
                const response = await api.get("/warehouses");
                setWarehouses(response.data);
            } catch (error) {
                console.error("Failed to fetch warehouses:", error);
            }
        };

        fetchWarehouses();
    }, []);

    const onSubmit = async (values: Resource) => {
        try {
            const message = encodeURIComponent(t("resources.resourceSuccess"));
            const response = await api.post("/resources", values, {
                headers: {
                    "message": message,
                }
            });
            console.log("Resource added:", response.data);

            if (location.pathname.includes('/organization/resources/create')) {
                navigate('/organization/resources');
            } else if (location.pathname.includes('/donor/resources/create')) {
                navigate('/donor');
            } else if (location.pathname.includes('/authority/resources/create')) {
                navigate('/authority/resources');
            } else {
                navigate('/');
            }
        } catch (error) {
            console.error("Failed to add resource:", error);
        }
    };

    return (
        <div className="max-w-lg mx-auto p-6 bg-white shadow-md rounded">
            <h2 className="text-xl font-semibold text-center mb-4">{t("resources.addResource")}</h2>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                <div>
                    <label htmlFor="resourceName" className="block text-sm font-medium text-gray-700">
                        {t("resources.resourceName")}
                    </label>
                    <input
                        id="resourceName"
                        {...form.register("resourceName", { required: t("resources.resourceNameRequired") })}
                        placeholder={t("resources.enterResourceName")}
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring focus:ring-blue-200"
                    />
                    {form.formState.errors.resourceName && (
                        <p className="text-sm text-red-600 mt-1">{form.formState.errors.resourceName.message}</p>
                    )}
                </div>

                <div>
                    <label htmlFor="resourceType" className="block text-sm font-medium text-gray-700">
                        {t("resources.resourceType")}
                    </label>
                    <input
                        id="resourceType"
                        type="text"
                        {...form.register("resourceType", { required: t("resources.resourceTypeRequired") })}
                        placeholder={t("resources.enterResourceType")}
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring focus:ring-blue-200"
                    />
                    {form.formState.errors.resourceType && (
                        <p className="text-sm text-red-600 mt-1">{form.formState.errors.resourceType.message}</p>
                    )}
                </div>

                <div>
                    <label htmlFor="resourceQuantity" className="block text-sm font-medium text-gray-700">
                        {t("resources.quantity")}
                    </label>
                    <input
                        id="resourceQuantity"
                        type="number"
                        {...form.register("resourceQuantity", { required: t("quantityRequired") })}
                        placeholder={t("resources.enterQuantity")}
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring focus:ring-blue-200"
                    />
                    {form.formState.errors.resourceQuantity && (
                        <p className="text-sm text-red-600 mt-1">{form.formState.errors.resourceQuantity.message}</p>
                    )}
                </div>

                <div>
                    <label htmlFor="warehouseId" className="block text-sm font-medium text-gray-700">
                        {t("resources.warehouse")}
                    </label>
                    <select
                        id="warehouseId"
                        {...form.register("warehouseId", { required: t("resources.warehouseRequired") })}
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring focus:ring-blue-200"
                    >
                        {warehouses.map((warehouse) => (
                            <option key={warehouse.warehouseId} value={warehouse.warehouseId}>
                                {warehouse.warehouseName} ({warehouse.location})
                            </option>
                        ))}
                    </select>
                    {form.formState.errors.warehouseId && (
                        <p className="text-sm text-red-600 mt-1">{form.formState.errors.warehouseId.message}</p>
                    )}
                </div>

                <div className="flex justify-center">
                    <button
                        type="submit"
                        className="px-4 py-2 bg-blue-600 text-white rounded shadow hover:bg-blue-700 focus:outline-none focus:ring focus:ring-blue-200"
                    >
                        {t("resources.addResource")}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default ResourceForm;
