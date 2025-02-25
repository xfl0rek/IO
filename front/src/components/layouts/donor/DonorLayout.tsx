import {Container} from '@mui/material'
import {ReactNode, useEffect} from 'react'
import { Pathnames } from '../../../router/pathnames'
import { useNavigate } from 'react-router-dom'
import "./styles/TopMenu.css";
import "./DonorLayout.css"
import {useTranslation} from "react-i18next";
import './i18n';
import {useAccount} from "@/contexts/uwierzytelnianie/AccountContext.tsx";

interface LayoutProps {
    children: ReactNode
}

export const DonorLayout = ({ children }: LayoutProps) => {
    const navigate = useNavigate()
    const { logout } = useAccount();

    const { i18n, t } = useTranslation();

    useEffect(() => {
        const handleLanguageChange = () => {
            const detectedLanguage = navigator.language.split("-")[0]; // np. "en" lub "pl"
            if (i18n.language !== detectedLanguage) {
                i18n.changeLanguage(detectedLanguage);
            }
        };

        // Wywołanie przy pierwszym renderowaniu
        handleLanguageChange();

        // Nasłuchiwanie zmian języka przeglądarki
        window.addEventListener("languagechange", handleLanguageChange);

        return () => {
            window.removeEventListener("languagechange", handleLanguageChange);
        };
    }, [i18n]);

    return (
        <div className="donor-body">
            <nav className="top-menu">
                <button className="menu-button" onClick={() => navigate(Pathnames.donor.homePage)}>
                    {t("donor.donate")}
                </button>
                <button className="menu-button" onClick={() => navigate(Pathnames.donor.financialDonations)}>
                    {t("donor.showFinancialDonations")}
                </button>
                <button className="menu-button" onClick={() => navigate(Pathnames.donor.itemDonations)}>
                    {t("donor.showItemDonations")}
                </button>
                <button className="menu-button" onClick={() => navigate(Pathnames.report.homePage)}>
                    {t("donor.report")}
                </button>
                <button className="menu-button" onClick={() => navigate(Pathnames.donor.accountPage)}>
                    {t("donor.myAccount")}
                </button>
                <button className="menu-button" onClick={() => { logout(); navigate('/')}}>
                    {t("donor.logOut")}
                </button>
            </nav>
            <Container sx={{p: 2, marginTop: 10}}>
                {children}
            </Container>
        </div>
    )
}
