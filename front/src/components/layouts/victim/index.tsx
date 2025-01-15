import { AppBar, Button, Container, Toolbar } from '@mui/material'
import { ReactNode } from 'react'
import { Pathnames } from '../../../router/pathnames'
import { useNavigate } from 'react-router-dom'
import { useAccount } from '../../../contexts/uwierzytelnianie/AccountContext'
import {useTranslation} from "react-i18next";
import "./i18n";

interface LayoutProps {
    children: ReactNode
}

export const VictimLayout = ({ children }: LayoutProps) => {
    const navigate = useNavigate()
    const { logout } = useAccount();
    const { t } = useTranslation();

    return (
        <div>
            <AppBar position="static">
                <Toolbar sx={{ display: 'flex'}}>
                    <Button onClick={() => navigate(Pathnames.victim.homePage)} sx={{ my: 2, mx: 2, color: 'white' }}>
                        Home
                    </Button>
                    <Button onClick={() => navigate(Pathnames.victim.accountPage)} sx={{ my: 2, mx: 2, color: 'white' }}>
                        My Account
                    </Button>
                    <Button onClick={() => navigate(Pathnames.victim.resources)} sx={{ my: 2, mx: 2, color: 'white' }}>
                        {t("resourcesList")}
                    </Button>
                    <Button onClick={() => { logout(); navigate('/')}} sx={{ my: 2, mx: 2, color: 'white' }}>
                        Logout
                    </Button>
                </Toolbar>
            </AppBar>
            <Container sx={{ p: 2 }}>
                {children}
            </Container>
        </div>
    )
}
